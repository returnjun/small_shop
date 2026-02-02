# 基础镜像
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:8-jre-slim

# 作者
MAINTAINER xiaofuge

# 配置
ENV PARAMS=""

# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD ./s-pay-mall-web/target/s-pay-mall-mvc-app.jar /s-pay-mall-mvc-app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /s-pay-mall-mvc-app.jar $PARAMS"]