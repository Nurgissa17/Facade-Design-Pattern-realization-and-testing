## Reset persisted state

Delete the CSV files inside the `data` folder. The application creates them again automatically.

## Principle of Least Knowledge

### Before Facade

Without the Facade, the Client would have to work with all three subsystems directly.

**`MatchmakingFacade.java | 39-41`**

```java
PartyFormation party = new PartyFormation(id, skillRating.averageRating(ratings), PartyFormation.Status.NEW);
party.form();
parties.save(party);
```

This shows the work with **Subsystem A**: create the party, form it, and save its state.

**`MatchmakingFacade.java | 55-57`**

```java
ServerAllocator server = new ServerAllocator(id, ping, ServerAllocator.Status.NEW);
server.allocate();
servers.save(server);
```

This shows the work with **Subsystem B**: create the server allocation, allocate it, and save its state.

**`MatchmakingFacade.java | 62`**

```java
GameSession session = new GameSession(id, GameSession.Status.NEW);
```

This creates the session object for **Subsystem C**.

**`MatchmakingFacade.java | 69-70`**

```java
session.confirm();
sessions.save(session);
```

This completes **Subsystem C** by confirming the session and saving its state.

Without the Facade, the Client would need to know and coordinate all these subsystem classes itself.

### After Facade

**`Main.java | 93`**

```java
facade.findMatch(matchId, ratings, latencyMs, failC);
```

This shows the **After** case: the Client makes one call to `MatchmakingFacade`, and the Facade coordinates Subsystems A, B, and C.

## Facade vs. Adapter

This project uses **Facade** because the goal is to provide one simple interface for the matchmaking workflow across three subsystems. `MatchmakingFacade` coordinates Party Formation, Server Allocation, and Game Session creation.

**Adapter** is used to make incompatible interfaces work together. The subsystem interfaces in this project are already usable, so the problem is coordination, not interface compatibility. Therefore, **Facade** is the correct pattern.
