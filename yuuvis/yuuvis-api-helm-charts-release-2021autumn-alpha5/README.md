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

# yuuvis api helm charts

## Prerequisites

Please use helm version [v3.2.4](https://github.com/helm/helm/releases/tag/v3.2.4), newer versions may not be compatible with some of the helm charts.

## Installing the Monitoring Helm chart

Installing monitoring chart
```

helm install monitoring ./monitoring -n monitoring --create-namespace --debug
```

Further information on configuration and available dashboards can be found in the [monitoring module readme](monitoring/README.md).

## yuuvis installation

First please add your credentials for the docker.yuuvis.org registry in the values yaml files of the helm charts.  For any questions about credentials please contact support@yuuvis.com.

Replace all **changeme** default passwords in the values.yaml of the charts you plan to use.   

### Add required Helm repositorys:

```shell
helm repo add minio https://helm.min.io/
helm repo add bitnami https://charts.bitnami.com/bitnami
```
### Install the infrastructure Helm chart

#### Update infrastructure dependencies

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

```shell
kubectl create namespace infrastructure
helm install infrastructure ./infrastructure --namespace infrastructure --debug
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

### Install the yuuvis rendition Helm chart

install rendition services with:
```shell
kubectl get po -n yuuvis
helm install rendition ./rendition --namespace yuuvis
```

### Install the yuuvis management Helm chart

install management services with:
```shell
kubectl get po -n yuuvis
helm install management ./management --namespace yuuvis
```

The management helm chart contains services for managing tenants.  
It provides a tenant-management api and a tenant management console.  
By default the deployment of the tenant management console services is disabled.  
To deploy the services the parameter *yuuvis.management.console.deploy* must be set to *true* in the values.yaml.  

```javascript
yuuvis:
  management:
    console:
      deploy: true
```

For configuration of the tenant management console client please refer to:
[tenant management console client configuration](https://help.optimal-systems.com/yuuvis_develop/pages/viewpage.action?pageId=51839656)

## Version upgrades

The upgrade of the infrastructure chart is not supported at the moment.
For upgrading the yuuvis or monitoring components get the new Helm charts version, edit the values.yaml of each chart with your modifications and the upgrade the Helm deployments:
Check version of deployed helm chart

```shell
helm list -n yuuvis 
helm list -n monitoring
```

```shell
helm upgrade yuuvis ./yuuvis --namespace yuuvis 
helm upgrade client ./client --namespace yuuvis 
helm upgrade bpm ./bpm --namespace yuuvis
helm upgrade monitoring ./monitoring --namespace monitoring 

```
Check version of upgraded helm chart

```shell
helm list -n yuuvis 
```

### 2021 autumn

With this version changes to the authentication configuration and authentication kubernetes resources are mandatory.  
Please refer to the documentation for more details: [authentication configuration changes](https://help.optimal-systems.com/yuuvis_develop/display/YMY/Update+Instructions+2021+Autumn#UpdateInstructions2021Autumn-Actuator)  
The yuuvis helm chart contains a *pre-hook* **pre-upgrade-job-2021autumn** that attempts to change the authentication configuration in the git.  
This hook is executed when *helm upgrade* is called and before the kubernetes configuration resources are updated.  
The authentication manage endpoints are now exposed on separated port.  
This is configured in the *authentication-prod.yml* config file, which is stored in the git.  
With this version a special kubernetes resource *authentication-manage* is used to access the manage endpoints of the authentication service inside the cluster.  
The liveness and readiness probe target ports are changed in the authentication deployment.  
With the version **1.5.0-rc3** of the tenant-management-api service the kubernetes configuration provided with the yuuvis helm chart version **0.13.0** is mandatory.  

### past versions

#### 2021 summer

The configuration files will not be changed during an upgrade.  
Please follow the instructions provied at:

* [breaking changes](https://help.optimal-systems.com/yuuvis_develop/display/YMY/Breaking+Changes)
* [update instructions 2021 summer version](https://help.optimal-systems.com/yuuvis_develop/display/YMY/Update+Instructions+2021+Summer)

With the 2021 summer version the webhook type *dms.request.update.metadata* is deprecated.  
The type is still functional in this version, but will be removed in later versions.  
Please migrate your config to use the new webhook type *dms.request.objects.upsert.storage-before*.  

[deprecated webhook](https://help.optimal-systems.com/yuuvis_develop/pages/viewpage.action?pageId=40144034)

## Uninstall

```shell
 helm uninstall prometheus-operator --namespace infrastructure
 helm uninstall monitoring  --namespace monitoring
 helm uninstall infrastructure  --namespace infrastructure
 helm uninstall yuuvis  --namespace yuuvis
 helm uninstall client  --namespace yuuvis
 helm uninstall management  --namespace yuuvis
 helm uninstall bpm  --namespace yuuvis
```

```shell
kubectl delete crd alertmanagerconfigs.monitoring.coreos.com
kubectl delete crd alertmanagers.monitoring.coreos.com
kubectl delete crd podmonitors.monitoring.coreos.com
kubectl delete crd probes.monitoring.coreos.com
kubectl delete crd prometheuses.monitoring.coreos.com
kubectl delete crd prometheusrules.monitoring.coreos.com
kubectl delete crd servicemonitors.monitoring.coreos.com
kubectl delete crd thanosrulers.monitoring.coreos.com
```

```shell
kubectl delete statefulset elasticsearch -n infrastructure
kubectl delete statefulset rabbitmq -n infrastructure
kubectl delete jobs keycloakaddrole-yuuvis  -n infrastructure
kubectl delete jobs keycloak-create-selfsigned-cert -n infrastructure
kubectl delete job gogsrepo-init -n infrastructure
kubectl delete pvc gogs -n infrastructure
kubectl delete pv name(replace with pv from gogs --check value with kubectl get pv -n infrastructure) -n infrastructure
```

Before deleting the persistent volumes and persistent volume claims, please delete corresponding pods.
