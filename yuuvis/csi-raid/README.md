# create secret:
for a new key:
/Users/wewer/.ssh/id_rsa
#example
kubectl create secret generic ssh-keys --from-file=id_rsa=/path/to/.ssh/id_rsa --from-file=id_rsa.pub=/path/to/.ssh/id_rsa.pub

macpro:
kubectl create secret generic ssh-keys --from-file=id_rsa=/Users/wewer/.ssh/id_rsa -n kube-system
optimal:
kubectl create secret generic ssh-keys --from-file=id_rsa=/home/jwewer/.ssh/id_rsa -n kube-system


then copy the key to templates/csiraidsecret.yaml

# deploy the dynamic storage provisioner
kubectl create -f templates/csiraidsecret.yaml
kubectl create -f templates/class.yaml
kubectl create -f templates/rbac.yaml
kubectl create -f templates/csiraidconfigmap.yaml
kubectl create -f templates/deployment.yaml

# test the provisioner:
# it should create the SUCCESS-CSI file in the provisioned volume.
kubectl create -f test/minio-csiraid-claim.yaml


kubectl create -f test/test-csi-claim.yaml
kubectl create -f test/test-csi-pod.yaml

# delete the test pod and claim
kubectl delete -f test/test-csi-pod.yaml
kubectl delete -f test/test-csi-claim.yaml

# undeploy the dynamic storage provisioner
kubectl delete -f templates/deployment.yaml
kubectl delete -f templates/csiraidconfigmap.yaml
kubectl delete -f templates/class.yaml
kubectl delete -f templates/rbac.yaml
kubectl delete -f templates/csiraidsecret.yaml

kubectl delete -f test/minio-csiraid-claim.yaml

mount -t nfs 192.168.178.77:/mnt/optimal/nfs-csi-provisioner /var/lib/kubelet/pods/e02ffbc1-6bb2-4b1b-8f28-068c00c4b0d8/volumes/kubernetes.io~nfs/nfs-client-roo