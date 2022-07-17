## Drone/Docker cooperation

A drone CI pipeline can use the different stages of a Dockerfile to run tests, build, and deploy an image. A Dockerfile should contain stages for each of those targets. The following example uses the Dockerfile of this repository.

```dockerfile
FROM sbtscala/scala-sbt:11.0.15_1.7.1_2.12.16 AS test

... # Excluded for brevity

FROM sbtscala/scala-sbt:11.0.15_1.7.1_2.12.16 AS build

... # Excluded for brevity

FROM openjdk:17.0.2-slim AS final

... # Excluded for brevity

COPY --from=build /opt/template/target/scala-2.12/drone-sample-project-assembly-0.1.0.jar /opt/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app.jar"]
```

## Explaining .drone.yml

A drone configuration can contain multiple pipelines that can be ran under different circumstances. A good starting point can be two pipelines, one testing and building the image on each commit and one testing, building, and deploying the image after a successful pr to master.

### `on_commit` pipeline

All pipeline steps use the docker plugin and target different Dockerfile stages

```yml
kind: pipeline
type: docker
name: on_commit

steps:
  - name: test
    image: plugins/docker
    settings:
      target: test
      dry_run: true
      repo: registry.draive.gr/library/drone-template
      registry: registry.draive.gr
      tags:
        - latest

  - name: build
    image: plugins/docker
    settings:
      target: build
      dry_run: true
      repo: registry.draive.gr/library/drone-template
      registry: registry.draive.gr
      tags:
        - latest

  - name: discord_notification
    image: appleboy/drone-discord
    when:
      status:
        - failure
        - success
    settings:
      webhook_id:
        from_secret: DISCORD_WEBHOOK_ID
      webhook_token:
        from_secret: DISCORD_WEBHOOK_TOKEN

trigger:
  branch:
    exclude:
      - master
  event:
    - push
    - pull_request
```

**Steps**:
* `test`: 
  * `target: test`: targets the test stage of the Dockerfile which is configured to run tests.
  * `dry_run`: doesn't publish an image to a repo.


