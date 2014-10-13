echo "Please enter password"
read PWD


mvn wildfly:deploy -Dwildfly.hostname=msinm-demo.e-navigation.net -Dwildfly.port=9995 -Dwildfly.username=ci -Dwildfly.password=$PWD


