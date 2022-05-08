# to use storageclass managed-nfs-raid-storage, it use the image jw-cloud.org:8443/csi-raid:v0.0.34-SNAPSHOT

see remarks for nfs server in nfs-provisioner

on nfs server:
mkdir /mnt/optimal/nfs-csi-provisioner

helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system
to uninstall:
helm uninstall azure-storage --namespace kube-system  

# in all in macpro set as storage class:

managed-nfs-raid-storage

#delete all pvc and pvs
cd playbooks
ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -vvvv

############ use blob storage for minio ###########################

# in all in macpro set as storage class:

miniopersistencestorageClass: blob

in azure it may be nessaray to set:

in the storage account under networks set:

Alle Netzwerke

in the storage class storageclass-blob.yaml

- -o allow_other

cd playbooks
ansible-playbook -i macpro deleteyuuvis.yml -v
cd ..
helm uninstall azure-storage --namespace kube-system
helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system
cd playbooks
ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -vvvv

# to switch of the backup functionality set to false:

CSIRAID_ACTIVE

in:
csi-raid-deployment.yaml
csi-blob-controller.yaml

#############################################


3. create azure secret:
kubectl create secret generic azure-secret --from-literal azurestorageaccountname=azurecloadstorage --from-literal azurestorageaccountkey="v3Jf97LiGHOwudUCCU094Cc4D6Adg1O5TUkdUbKnqwUV99YFsJa3JNs3uClogNrMsBDD94LeRz5H0E7nS/yxjQ==" --type=Opaque
neu:
kubectl create secret generic azure-nfs3-secret --from-literal azurestorageaccountname=azurenfs3cloadstorage --from-literal azurestorageaccountkey="z2h7JY7GfvDmdhhUrNNBRAhAa+/26Zq/k52EP3sGqKqzK/hp7cwz2YMtXA9bvTzZXt0CoCXdKW676uQ6qC0Kwg==" --type=Opaque

kubectl create secret generic azure-cloud-provider --from-literal vnetResourceGroup=csiraid --from-literal vnetName=azurenfs3cloadstorage_vNet129929 --from-literal subnetName=default --type=Opaque --namespace=kube-system
kubectl create -f azure-cloud-provider.yaml
kubectl delete -f azure-cloud-provider.yaml


2. Install the CSI driver
helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system

to uninstall:
helm uninstall azure-storage --namespace kube-system

3. check if succeeded:
kubectl get CSIDriver


4. create storage class referencing azure-secret:
cd deploy
kubectl create -f storageclass-blob-secret.yaml
neu 
kubectl create -f storageclass-blob-nfs3-secret.yaml
kubectl delete -f storageclass-blob-nfs3-secret.yaml
kubectl create -f storageclass-blob-nfs.yaml

kubectl delete -f storageclass-blob-nfs.yaml


5. create an application using the storage class blob:
kubectl create -f ./example/statefulset.yaml
kubectl create -f ./example/statefulset-nfs.yaml

kubectl delete -f ./example/statefulset.yaml
kubectl delete -f ./example/statefulset-nfs.yaml

7. config map for raid:
csi-raid-blob-configmap
neu
csi-raid-blob-nfs3-configmap

8. in csi-blob-controler
csi-raid-blob-configmap
neu
csi-raid-blob-nfs3-configmap


cd playbooks
ansible-playbook -i macpro deleteyuuvis.yml -v    
ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -vvvv


cd playbooks
ansible-playbook -i macpro deleteyuuvis.yml -v    
cd ..
helm uninstall azure-storage --namespace kube-system
cd deploy
kubectl delete -f storageclass-blob-nfs.yaml
kubectl get pods -n infrastructure
kubectl delete pods elasticsearch-0 -n infrastructure --force=true
kubectl delete pods rabbitmq-0 -n infrastructure --force=true
kubectl delete pods minio-6b946fcf7c-w6rrb -n infrastructure --force=true
kubectl delete pods gogs-7fdb548486-j8j2l -n infrastructure --force=true
kubectl delete pods postgresql-0 -n infrastructure --force=true
kubectl get pods -n default
kubectl delete sfs statefulset-blob-nfs -n default --force=true
kubectl delete pods statefulset-blob-0 -n default --force=true
pvc auf lenz löschen
pvc auf azure löschen

cd deploy
kubectl create -f storageclass-blob-nfs3-secret.yaml
kubectl delete -f storageclass-blob-nfs3-secret.yaml
cd ..
helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system
cd playbooks
ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -vvvv
ansible-playbook -i macpro updatepostgreSQL.yml -vvvv

cd deploy
kubectl create -f storageclass-blob-nfs.yaml
kubectl create -f ./example/statefulset-nfs.yaml

mkdir -p /mnt/testnfs3
mount -o sec=sys,vers=3,nolock,proto=tcp azurenfs3cloadstorage.blob.core.windows.net:/azurenfs3cloadstorage/pvc-67fe7a47-2283-4ee1-8d95-2f59262e408d  /mnt/testnfs3
mount -o sec=sys,vers=3,nolock,proto=tcp azurenfs3cloadstorage.blob.core.windows.net:/pvc-67fe7a47-2283-4ee1-8d95-2f59262e408d  /mnt/testnfs3

mount -o sec=sys,vers=3,nolock,proto=tcp azurenfs3cloadstorage.blob.core.windows.net:/azurenfs3cloadstorage/pvc-28b9cb28-1ec5-45d8-83db-57317767e451  /mnt/testnfs3

kubectl create -f storageclass-blob-secret.yaml
kubectl create -f ./example/statefulset.yaml

# create secret:
for a new key:
/Users/wewer/.ssh/id_rsa
kubectl create secret generic ssh-keys --from-file=id_rsa=/path/to/.ssh/id_rsa --from-file=id_rsa.pub=/path/to/.ssh/id_rsa.pub
kubectl create secret generic ssh-keys --from-file=id_rsa=/Users/wewer/.ssh/id_rsa -n kube-system

helm install azure-storage azure-storage --set node.enableBlobfuseProxy=false --namespace kube-system
cd playbocks
ansible-playbook -i macpro updatepostgreSQL.yml -vvvv
ansible-playbook -i macpro deleteyuuvis.yml -v  
cd ..
helm uninstall azure-storage --namespace kube-system

source
/var/lib/kubelet/plugins/kubernetes.io/csi/pv/pvc-f6e0092e-eae8-40dd-8297-0bbeb1d85e45/globalmount

target
/var/lib/kubelet/pods/84248c06-15ed-49ac-9ca2-9bd03bc986ed/volumes/kubernetes.io~csi/pvc-f6e0092e-eae8-40dd-8297-0bbeb1d85e45/mount

volumeID
volumeId #azurenfs3cloadstorage#pvc-f6e0092e-eae8-40dd-8297-0bbeb1d85e45

mount -o azurenfs3cloadstorage.blob.core.windows.net:/azurenfs3cloadstorage/pvc-f6e0092e-eae8-40dd-8297-0bbeb1d85e45 /mnt/testmount

############## install on vm

apt-get install curl
curl -sSL https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
apt-add-repository https://packages.microsoft.com/debian/10/prod
apt-get update
apt-get install blobfuse

create a ram disk:
sudo mkdir /mnt/ramdisk
sudo mount -t tmpfs -o size=16g tmpfs /mnt/ramdisk
sudo mkdir /mnt/ramdisk/blobfusetmp

nano fuse_connection.cfg

accountName azurenfs3cloadstorage
accountKey z2h7JY7GfvDmdhhUrNNBRAhAa+/26Zq/k52EP3sGqKqzK/hp7cwz2YMtXA9bvTzZXt0CoCXdKW676uQ6qC0Kwg==
containerName postgresqlstorage

chmod 600 fuse_connection.cfg

mkdir mycontainer

blobfuse mycontainer --tmp-path=/mnt/resource/blobfusetmp  --config-file=fuse_connection.cfg -o gid=1001 -o uid=1001 -o attr_timeout=240 -o entry_timeout=240 -o negative_timeout=120

docker run --mount type=bind,src=/root/mycontainer,dst=/bitnami/postgresql --env POSTGRESQL_INITSCRIPTS_USERNAME=1001 --env POSTGRESQL_DATA_DIR=/bitnami/postgresql/data --env ALLOW_EMPTY_PASSWORD=yes --env BITNAMI_DEBUG=true c36aeca47db8
docker run -v /root/mycontainer:/bitnami/postgresql  --env POSTGRESQL_DATA_DIR=/bitnami/postgresql/data  --env ALLOW_EMPTY_PASSWORD=yes --env BITNAMI_DEBUG=true c36aeca47db8

-e POSTGRESQL_USERNAME=my_user -e POSTGRESQL_PASSWORD=password123
POSTGRESQL_DATA_DIR
umount /root/mycontainer

/opt/bitnami/scripts/postgresql/entrypoint.sh