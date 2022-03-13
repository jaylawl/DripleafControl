# DripleafControl

Allows you to determine if, and for how long, big dripleaf blocks will tilt.
Comes with configurable API event *EntityTriggerBigDripleafEvent* for advanced control.

Download a compiled version of the plugin here: https://www.spigotmc.org/resources/dripleafcontrol.100690/

### Commands

- "/dripleafcontrol" - Used to reload the plugin. Requires the permission "dripleafcontrol.admin"

### Permissions

- "dripleafcontrol.admin" - Required for the command "/dripleafcontrol"

## Default config.yml
Located in plugins default data folder
```
BigDripleaf:
  TiltProcess:
    TicksUntilState:
      UNSTABLE: 0
      PARTIAL: 10
      FULL: 20
      NONE: 30
```
### Editing config.yml
Should you put invalid data/values into the config.yml, there will be descriptive error messages in the console upon plugin-reload.

## Installation

- Put the plugin in your servers "plugins" folder
- Restart the server

## Deinstallation

- Remove the plugin from your servers "plugins" folder
- Restart the server