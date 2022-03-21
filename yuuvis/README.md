# License

Copyright 2021 OPTIMAL SYSTEMS GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# yuuvis api Helm Charts

## Prerequisites

we use version: v3.5.0
Please use helm version [v3.2.4](https://github.com/helm/helm/releases/tag/v3.2.4), newer versions may not be compatible with some of the helm charts.


## install persistent volumes in kubernetess

check disks:

on master:
lsblk
-> sdb1

list all mounted filesystems:
sudo mount -t ext4

to mount a disk on /dev/sdb1 on the directory /mnt/optimal
sudo mkdir -p /mnt/optimal
sudo mount -t ext4 /dev/sdb1 /mnt/optimal

ansible-playbook -i hosts ./playbooks/nfs/nfs-dependencies.yml -v --extra-vars "ansible_sudo_pass=OSVHyuuvis2021!"

ansible-playbook -i hosts ./playbooks/pvs.yml -v --extra-vars "ansible_sudo_pass=OSVHyuuvis2021!"

## yuuvis installation

First please add your credentials for the docker.yuuvis.org registry in the values yaml files of the helm charts.  For any questions about credentials please contact support@yuuvis.com.

Replace all **changeme** default passwords in the values.yaml of the charts you plan to use.   !! minimum length 8 Character (minio)

### Add required Helm repositorys:

```shell
helm repo add minio https://helm.min.io/
helm repo add bitnami https://charts.bitnami.com/bitnami
```
### Install the infrastructure Helm chart

#### Update infrastructure dependencies

old:
```shell
cd infrastructure
helm dep up
helm repo add stable https://kubernetes-charts.storage.googleapis.com/
cd ..
```

new:
https://kubernetes-charts.storage.googleapis.com/ is no longer available
instead:
helm repo rm stable

```shell
cd infrastructure
helm dep up
helm repo add stable https://charts.helm.sh/stable
cd ..
```

#### Edit the infrastructure values.yaml

* Edit the docker registry credentials. 
* Optionally change passwords
* Optionally change the used storage classes

#### Install infrastructure services

export KUBECONFIG=/Users/wewer/.kube/master/etc/kubernetes/admin.conf

```shell
kubectl create namespace infrastructure
helm install infrastructure ./infrastructure --namespace infrastructure
```

wait till jobs are done

```shell
kubectl get jobs -n infrastructure
```

There are 2 jobs that prepare the git server and the keycloak environment that need to be completed.

```shell
NAME                              COMPLETIONS   DURATION   AGE
gogsrepo-init                     1/1           83s        8m4s
keycloak-create-selfsigned-cert   1/1           8m4s       8m4s
```

#### Edit the yuuvis values.yaml

* Edit the docker registry credentials.

check port of keycloak:
look for service:
keycloak-https ....NodePort....443/30111/TCP

check keycloak endpoint: https://<master-ip>:30111

### Install the yuuvis Helm chart

```shell
kubectl create namespace yuuvis
helm install yuuvis ./yuuvis --namespace yuuvis
```

wait till all pods are ready 

```shell
kubectl get po -n yuuvis
```

#### Edit the client values.yaml

* Edit the docker registry credentials.

### Install the yuuvis client Helm chart:

```shell
helm install client ./client --namespace yuuvis
```

wait till all pods are ready 

```shell
kubectl get po -n yuuvis
```

#### Post-install tasks for the client

The client helm chart will change the *systemHookConfiguration.json*.  
Services that use this configuration will only read it once at startup.  
For the changes to be noticeable the corresponding services must be restart.  
The changes in the *systemHookConfiguration.json* affect the api gateway.  
To restart the api gateway:  

```shell
kubectl rollout restart deployment api -n yuuvis
```

A role *YUUVIS_CREATE_OBJECT* must be created and assigned to users who should be able to create objects in the client.  

#### Edit the bpm values.yaml

* Edit the docker registry credentials.


### Install the yuuvis bpm Helm chart

install bpm services with:
```shell
kubectl get po -n yuuvis
helm install bpm ./bpm --namespace yuuvis
```

## Version upgrades

The upgrade of the infrastructure chart is not supported at the moment.

For upgrading the yuuvis components get the new Helm charts version, edit the values.yaml of each chart with your modifications and the upgrade the Helm deployments:

Check version of deployed helm chart

```shell
helm list -n yuuvis 
```


```shell
helm upgrade yuuvis ./yuuvis --namespace yuuvis 
helm upgrade client ./client --namespace yuuvis 
helm upgrade bpm ./bpm --namespace yuuvis 
```
Check version of upgraded helm chart

```shell
helm list -n yuuvis 
```

## Uninstall

```shell

 helm uninstall infrastructure  --namespace infrastructure
 helm uninstall yuuvis  --namespace yuuvis
 helm uninstall client  --namespace yuuvis
 helm uninstall bpm  --namespace yuuvis

```

```shell
kubectl delete statefulset elasticsearch -n infrastructure
kubectl delete statefulset rabbitmq -n infrastructure
kubectl delete jobs keycloakaddrole-yuuvis  -n infrastructure
kubectl delete jobs keycloak-create-selfsigned-cert -n infrastructure
kubectl delete job gogsrepo-init -n infrastructure
kubectl delete pvc gogs -n infrastructure
kubectl delete pv name(replace with pv from gogs --check value with kubectl get pv -n infrastructure) -n infrastructure

kubectl delete pv -l app=pvcs 
```
While deleting persistence volume and persistence claim from gogs, please delete pod, than they will be unbind.



use kubernetes helm repos in ansible
ansible-galaxy collection install community.kubernetes

in playbooks use:
community.kubernetes.helm_repository

####################################################
1. install yuuvis
####################################################

cd infrastructure

In der Chart.yaml Datei werden die abhängigen Pakete spezifiziert, mit Version

dann:
helm dep up

cd playbooks

################## install with predefinied persistent volumes ############################

ansible-playbook -i macpro storage.yml -v
ansible-playbook -i macpro update.yml -v


ansible-playbook -i macpro delete.yml -v

# if the volumes have to be recreated use start if existing volumes should be use use update
ansible-playbook -i macpro start.yml -v
ansible-playbook -i macpro update.yml -v

# if dynamic storage provisioning (nfs or blob) should be used
#thats no longer necesary:
helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system
#instead just call:

# if it is deployed first time on the cluster: create secret:
for a new key:
/Users/wewer/.ssh/id_rsa
#example
kubectl create secret generic ssh-keys --from-file=id_rsa=/path/to/.ssh/id_rsa --from-file=id_rsa.pub=/path/to/.ssh/id_rsa.pub

macpro:
kubectl create secret generic ssh-keys --from-file=id_rsa=/Users/wewer/.ssh/id_rsa -n kube-system
optimal:
kubectl create secret generic ssh-keys --from-file=id_rsa=/home/jwewer/.ssh/id_rsa -n kube-system

then copy the key to templates/csiraidsecret.yaml


ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -v
optimal:
ansible-playbook -i optimal updateForCsiRaidProvisioner.yml -v

uninstall yuuvis and dynamic storage provisioner:
ansible-playbook -i macpro deleteyuuvis.yml -v
ansible-playbook -i optimal deleteyuuvis.yml -v

uninstall just the dynamic storage provisioner
helm uninstall azure-storage --namespace kube-system

for blob storage class:
edit the all file in macpro and fill inthe blob class

for optimal:
ansible-playbook -i optimal deleteyuuvis.yml -v
ansible-playbook -i optimal start.yml -v
ansible-playbook -i optimal update.yml -v

for azure:
ansible-playbook -i azure deleteyuuvis.yml -v
ansible-playbook -i azure update.yml -v


if delete not works:
kubectl delete pods --all -n infrastructure --force
kubectl delete pods --all -n yuuvis --force

if helm blocks:
helm history yuuvis -n yuuvis
helm delete yuuvis 1 -n yuuvis

! vorher gg verzeichnis auf root:root zurücksetzen:
sudo chown root:root /mnt/optimal/gg
ansible-playbook -i optimal start.yml -v

######################################################
2. keycloak konfigurieren:
######################################################
https://<master-ip>:30111
keycloak
optimalsystem

Master:
im Login Bereich SSL - none auswählen

yuuvis login
http://10.211.55.4:30080/
root
optimalsystem

-> Postman request funktionieren:

######################################################
3. userservice neustarten
######################################################
online login läuft in schleife:
im api-web sieht man:
2021-04-22 07:00:35.831 INFO 1 --- [qtp461591680-17] c.o.s.utils.error.ServiceExceptionUtils : 500 Internal Server Error ServiceErrorResult(errorMessage=500 Internal Server Error, messageHistory=null, stackTrace=[com.os.services.zerberus.rest.UserController.whoAmI(:0)], service=api-web, tenant=yuuvistest, suppressed=[], httpHeaders={}, httpErrorCode=422)

im userservice Deployment:

bei:
env:
- name: JAVA_OPTS
    value: '-Xmx128m -XX:+ExitOnOutOfMemoryError'

den Parameter -Dlogging.level.root=TRACE wie folgt einfügen.
- name: JAVA_OPTS
  value: '-Xmx128m -XX:+ExitOnOutOfMemoryError -Dlogging.level.root=TRACE'
  
dann den Pod userservice löschen.

-> online login funktioniert.

manchmal ist es auch der authentication service (Fehler im log zu sehen: cannot find user ...) man bekommt einen unauthorized zurück,
dann gleiches wie mit user-service machen

##########################################################
minio speicher 
##########################################################
kubectl port-forward minio-6b946fcf7c-w6rrb 9000:9000 --namespace=infrastructure
http://localhost:9000
und dann die minio secrets
###########################################################
swagger ui zugreifen:
#####################################################
api-web
kubectl port-forward api-web-57b6fdcf59-4fsxd 7550:7550 --namespace=yuuvis
http://localhost:7550/swagger-ui.html

kubectl port-forward api-6c7f7cc76c-qs9mb 7550:7550 --namespace=yuuvis

#####################################################

neue rolle anlegen und hinzufügen(wird in aktueller konfiguration default mäßig mit angelegt):
YUUVIS_CREATE_OBJECT

########################################################
admin oberfläche - check ob alle services oben sind
#########################################################
kubectl port-forward admin-55f7c7fd7-dl72j 7273:7273 --namespace=yuuvis
localhost:7273

gogs:
kubectl port-forward gogs-7fdb548486-jmbx8 3000:3000 --namespace=infrastructure
localhost:3000
login mit yuuvis

kubectl get pods/userservice-6bb6d648b5-j8qqv --namespace=yuuvis -o yaml | grep "serviceAccount" 

#######################################################
deployment Änderungen
#######################################################
1. Anzahl Felder auf 100 erhöhen:

yuuvis/values.yaml
unter:   system-prod:
die folgende Zeile hinzufügen:
schema.tenant.properties.limit: 100


2. Deutsche Feldnamen: - entfällt in 2021summer

client/templates/values
unter:   i18nde:
inhalt von config/i18n_de.json eingefügt

3. Yuuvis Create Rechte: - entfällt in 2021summer

infrastructure/templates/realmconfigmap.yaml

in realmRoles die Rolle: YUUVIS_CREATE_OBJECT
hinzugefügt

#################################################################
#       log level
#################################################################

in dem deployments (geht bei allen), in den JAVA_OPTS folgenden Parameter hinzufügen:

-Dlogging.level.root=TRACE


################################################################


{
    "query": {
    "statement": "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorname = 'Tino'",
    "skipCount": 0,
    "maxItems": 50
    }
}


####################  csi-raid ##############################

helm install csi-raid csi-raid
helm uninstall csi-raid


to test the provisioner:

kubectl create -f csi-raid/test/test-csi-claim.yaml
kubectl create -f csi-raid/test/test-csi-pod.yaml

it should create the SUCCESS-CSI file in the provisioned volume.

| grep managed-nfs-raid-storage