# This is a helm chart for deploying a simple nfs provisioner - based on Kubernetes imgae

# requirements on nfs

# the /etc/expots should contain:

/mnt       192.0.0.0/255.0.0.0(rw,sync,no_subtree_check,no_root_squash)
/mnt       11.12.0.0/255.255.0.0(rw,sync,no_subtree_check,no_root_squash)

# important is no_root_squash for gogs

# if you have edit the /etc/exports

systemctl status nfs-server
systemctl restart nfs-kernel-server

# for a separate test try this

kubectl create -f templates/rbac.yaml

Edit deploy/deployment.yaml and replace the two occurences of with your server's hostname.

kubectl create -f templates/deployment.yaml
kubectl create -f templates/class.yaml

to test the provisioner:

kubectl create -f test/test-claim.yaml
kubectl create -f test/test-pod.yaml

it should create the SUCCESS file in the provisioned volume.

# for a test with yuuvis try this

helm install nfs-provisioner nfs-provisioner --namespace kube-system
helm uninstall nfs-provisioner --namespace kube-system

# in all in macpro set as storage class:

managed-nfs-storage

like:
elasticsearchstorageClassName: managed-nfs-storage

the deploymentpath of the persistentvolums is specified in the value.yaml, check that the path exist und clean it

ansible-playbook -i macpro updateForCsiRaidProvisioner.yml -vvvv
ansible-playbook -i macpro deleteyuuvis.yml -v