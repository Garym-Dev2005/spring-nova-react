# Based on https://docs.aws.amazon.com/emulator/latest/userguide/getting-started-docker.html
# (no prebuilt image is published). uname -m picks the x86_64 or aarch64 RPM.
FROM public.ecr.aws/amazonlinux/amazonlinux:2023-minimal
RUN rpm -i "https://downloads.aws.com/aws-emulator/packages/aws-emulator/LTS/latest/aws-emulator-$(uname -m).rpm" \
    && mkdir -p /home/vscode/.aws/emulator \
    && chown -R 1000:1000 /home/vscode
USER 1000
ENV HOME=/home/vscode
ENTRYPOINT ["aws-emulator"]
