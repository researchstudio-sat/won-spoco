# won-spoco
Spontaneous Cooperation

## How to Deploy

1. Clone the repository
2. Use `scripts/deploy-template.sh` to create your own deploy script
3. Edit `conf/raid-bot.properties` to include all necessary information
4. Edit other config files as needed
5. Run `mvn package`
6. Set the necessary environment variables with `export DOCKER_CERT_PATH=/root/docker-deploy-keys`
7. Run your deploy script
   