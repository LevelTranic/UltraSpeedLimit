# UltraSpeedLimit
Limit the speed at which players can switch between servers.

## Required
- Java 17+
- Velocity 3.3.0+
- [Plugin|Lib] [MavenLoaderAPI 1.0.1+](https://github.com/LevelTranic/MavenLoader)

## Config
```yaml
connections: 2 #The number of times that can be switched between the server during the expiration time+1
expired-connections: 30 #Expired time, unit: second
connect-failed-message: The connection speed limit has been reached! Please come back
  later!
```