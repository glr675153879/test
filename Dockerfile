FROM hscloud/java:8-jre

MAINTAINER wangiegie@gmail.com

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /hs-cost-account

WORKDIR /hs-cost-account

#EXPOSE 5001

ADD ./target/hs-cost-account.jar ./

#CMD sleep 60;java $JAVA_OPTS -jar hs-cost-account.jar
CMD java $JAVA_OPTS -jar hs-cost-account.jar
