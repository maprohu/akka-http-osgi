
repo-add mvn:com.github.maprohu/akka-http-osgi/0.1.0-SNAPSHOT/xml/features

SSL:
https://github.com/akka/akka/pull/18200/files

Linux Port Forward:
iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 443 -j REDIRECT --to-port 8443
