echo "Please enter password"
read PWD


mvn wildfly:deploy -Dwildfly.hostname=msinm-demo.e-navigation.net -Dwildfly.port=9994 -Dwildfly.username=ci -Dwildfly.password=$PWD


