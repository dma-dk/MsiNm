echo "Please enter password"
read PWD


mvn wildfly:deploy -Dwildfly.hostname=alpha.e-navigation.net -Dwildfly.port=9990 -Dwildfly.username=ci -Dwildfly.password=$PWD


