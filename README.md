This repository is used to support members of Draive (https://draive.gr).

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

* `test` 
  * `target`: targets the test stage of the Dockerfile which is configured to run tests.
  * `dry_run`: doesn't publish an image to a repo.
  * `repo`: the repository containing this project's images. Isn't used during a dry run but still needs to be present.
  * `registry`: the registry containing this project's images. Isn't used during a dry run but still needs to be present.
  * `tags`: tag of the image.

* `build`
  * `target`: targets the build stage of the Dockerfile which is configured to build the project.
  * `dry_run`: doesn't publish an image to a repo.
  * `repo`: the repository containing this project's images. Isn't used during a dry run but still needs to be present.
  * `registry`: the registry containing this project's images. Isn't used during a dry run but still needs to be present.
  * `tags`: tag of the image.

* `discord_notification`
  * `when`: sends the notification only on build success and failure.
  * `settings`: configures the discord channel to which the notification will be sent. These values are retrieved from secrets configured on the drone server.

* `target`: this pipeline is ran on every pull request and any push to any branch other than master.

### `on_push_master` pipeline

```yml
kind: pipeline
type: docker
name: on_push_master

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

  - name: publish
    image: plugins/docker
    settings:
      target: final
      repo: registry.draive.gr/library/drone-template
      registry: registry.draive.gr
      tags:
        - latest
      insecure: true
      username:
        from_secret: REGISTRY_USERNAME
      password:
        from_secret: REGISTRY_PASSWORD

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
    - master
  event:
    - push
```

* `test` 
  * `target`: targets the test stage of the Dockerfile which is configured to run tests.
  * `dry_run`: doesn't publish an image to a repo.
  * `repo`: the repository containing this project's images. Isn't used during a dry run but still needs to be present.
  * `registry`: the registry containing this project's images. Isn't used during a dry run but still needs to be present.
  * `tags`: tag of the image.

* `publish`
  * `target`: targets the final stage of the Dockerfile which is configured to package the output of the build stage into a production-ready image.
  * `repo`: the repository containing this project's images.
  * `registry`: the registry containing this project's images.
  * `insecure`: the registry and the drone server are on the same private network, so https communication isn't necessary.
  * `username` and `password`: credentials of the registry user that is used to push the images. The values are retrieved from secrets on the drone server. 
  * `tags`: tag of the image.

* `discord_notification`
  * `when`: sends the notification only on build success and failure.
  * `settings`: configures the discord channel to which the notification will be sent. These values are retrieved from secrets configured on the drone server.

* `target`: this pipeline is ran on every push to master.
