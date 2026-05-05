# PFE1 - Microservices Project

A Spring Boot microservices architecture with Eureka discovery, API Gateway, and various business services.

## Architecture

- **Discovery Service**: Eureka server for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and load balancing
- **User Service**: User management and authentication
- **Dossier Service**: Dossier management
- **Workflow Service**: BPM orchestration with Camunda
- **Paiement Service**: Payment processing
- **Notification Service**: Notification handling
- **Document Service**: File management
- **Frontend**: Vue.js application

## Prerequisites

- Docker and Docker Compose
- Java 17
- Maven

## Local Development

1. Clone the repository
2. Run `docker-compose up` to start all services including monitoring
3. Access:
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)
   - PGAdmin: http://localhost:5050

## Monitoring

The project includes Prometheus and Grafana for monitoring:

- **Prometheus**: Scrapes metrics from all services at `/actuator/prometheus`
- **Grafana**: Visualizes metrics with pre-configured dashboards

## Deployment

### Docker Compose

```bash
docker-compose up -d
```

### Kubernetes

```bash
kubectl apply -f k8s/
```

### Render.com

1. Connect your repository to Render
2. Use the `render.yaml` configuration for multi-service deployment
3. Services will be deployed with monitoring included

## Configuration

- Database: PostgreSQL
- JWT for authentication
- Eureka for service discovery

## Building

Each service can be built individually:

```bash
cd <service-name>
mvn clean package
```

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
