#deployment repository for produktion

cd yuuvis/playbooks
ansible-playbook -i optimal deleteyuuvis.yml -v
cd elastichelmcharts/playbooks
ansible-playbook -i optimal elkDelete.yml -v



git clone https://github.com/JuergenWewer/deploy-produktion.git
cd deploy-production
copyadmin.sh
git add .
git commit -m "admin.conf"
git push


store the kubernetes admin.conf for the cluster in deploymentagent/admin.conf
configure deployment repository in:

deploymentagent/playbooks.yaml
deploymentagent/deployment.yaml
deploymentagent/host/group_vars/all

the host directory contains the hosts and the variables in all

deploy the agent to the cluster:
export KUBECONFIG=/home/jwewer/workspace/deploy-produktion/deploymentagent/admin.conf

kubectl create -f deploymentagent/deployment.yaml
kubectl delete -f deploymentagent/deployment.yaml