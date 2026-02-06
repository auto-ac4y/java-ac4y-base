# java-ac4y-base - Architektúra Dokumentáció

## Áttekintés

Az `ac4y-base` modul az Ac4y keretrendszer mag komponense. Process-alapú feldolgozást, kivétel kezelést, konfigurációs szolgáltatásokat és HTTP szolgáltatást biztosít.

**Verzió:** 1.1.0
**Java verzió:** 11
**Szervezet:** ac4y-auto

## Fő Komponensek

### 1. Process Kezelés

#### `Ac4yProcess`
Az Ac4y keretrendszer központi feldolgozó osztálya. Template pattern implementáció.

**Annotációk:**
- `@XmlRootElement`: JAXB támogatás XML szerializációhoz

**Fő metódus:**
```java
public Object process(Object aObject) throws ClassNotFoundException, Ac4yException,
    SQLException, IOException, ParseException
```

**Működési elv:**
- Leszármazott osztályok felülírják a `process()` metódust
- Bemeneti objektum feldolgozása
- Kimeneti objektum visszaadása
- Többféle kivételt dob: ClassNotFoundException, Ac4yException, SQLException, IOException, ParseException

**Használat:**
```java
public class MyProcessor extends Ac4yProcess {
    @Override
    public Object process(Object input) throws Ac4yException {
        // Feldolgozási logika
        return result;
    }
}
```

#### `Ac4yProcessContext`
Context objektum a feldolgozási folyamatokhoz. Külső és belső adatok elkülönített tárolása.

**Mezők:**
- `external` (Object): Külső kontextus (pl. HTTP request, user session)
- `internal` (Object): Belső kontextus (pl. cache, shared state)

**Metódusok:**
- `getExternal()` / `setExternal(Object external)`
- `getInternal()` / `setInternal(Object internal)`

**Konstruktorok:**
- `Ac4yProcessContext()`: Üres kontextus
- `Ac4yProcessContext(Object external, Object internal)`: Inicializált kontextus

**Használati minta:**
```java
Ac4yProcessContext context = new Ac4yProcessContext(httpRequest, cacheData);
Object externalData = context.getExternal();
Object internalData = context.getInternal();
```

### 2. Kivétel Kezelés

#### `Ac4yException`
Az Ac4y keretrendszer saját kivétel osztálya.

**Extends:** `java.lang.Exception`

**Konstruktorok:**
- `Ac4yException()`: Üres kivétel
- `Ac4yException(String message)`: Üzenettel

**Speciális metódus:**
- `getStackTraceAsString(Throwable throwable)`: Stack trace String formátumban
  - Hasznos logging és debug célokra
  - PrintWriter használatával konvertálja

**Használat:**
```java
throw new Ac4yException("Hibás konfiguráció");

try {
    // kód
} catch (Exception e) {
    Ac4yException ae = new Ac4yException();
    String stackTrace = ae.getStackTraceAsString(e);
    // log stackTrace
}
```

#### `ErrorHandler`
Egyszerű static error handler logging célokra.

**Static metódusok:**
- `addStack(Exception exception)`: Exception stack trace nyomtatása
  - System.out.println használatával
  - Format: "addStack: " + exception.toString()

- `addMessage(String message)`: Egyszerű üzenet nyomtatása
  - Format: "addMessage: " + message

**Használat:**
```java
try {
    // kód
} catch (Exception e) {
    ErrorHandler.addStack(e);
}

ErrorHandler.addMessage("Process started");
```

**Megjegyzés:** Egyszerű console logging, éles környezetben logger framework ajánlott (log4j, slf4j).

### 3. Konfiguráció Kezelés

#### `ExternalPropertyHandler`
Properties fájlok betöltése classpath-ról.

**Fő metódus:**
- `getPropertiesFromClassPath(String propertiesName)`: Properties betöltése
  - Classpath-ról tölt be .properties fájlt
  - IOException dobható
  - Ac4yException dobható hibás betöltés esetén

**Használat:**
```java
ExternalPropertyHandler eph = new ExternalPropertyHandler();
Properties props = eph.getPropertiesFromClassPath("database.properties");
String dbUrl = props.getProperty("db.url");
```

**Tipikus properties formátum:**
```properties
db.url=jdbc:mysql://localhost:3306/mydb
db.user=admin
db.password=secret
```

### 4. HTTP Szolgáltatás

#### `Ac4yHttpService`
HTTP kérések kezelésére szolgáló szolgáltatás (file-ban van, de részletes implementáció nem került beolvasásra).

**Package:** `ac4y.base.http`

**Várható funkcionalitás:**
- HTTP kérések küldése
- HTTP válaszok fogadása
- REST API kommunikáció

## Függőségek

### Maven Függőség

```xml
<dependency>
    <groupId>ac4y</groupId>
    <artifactId>ac4y-utility</artifactId>
    <version>1.0.0</version>
</dependency>
```

Az `ac4y-base` függ az `ac4y-utility` modultól, így közvetetten használja:
- StringHandler, DateHandler, GUIDHandler
- XMLHandler, GsonCap, JaxbCap

## Architektúra Minták

### 1. Template Method Pattern
`Ac4yProcess` osztály template method mintát implementál:
```java
Ac4yProcess base = new MyConcreteProcess();
Object result = base.process(input);
```

### 2. Context Object Pattern
`Ac4yProcessContext` külön tárolja a külső és belső kontextust:
```java
Ac4yProcessContext context = new Ac4yProcessContext(external, internal);
myProcess.setContext(context);
```

### 3. Strategy Pattern
`Ac4yProcess` leszármazottak különböző feldolgozási stratégiákat implementálhatnak.

## Felhasználási Minták

### 1. Egyszerű Process Implementáció

```java
public class DataTransformer extends Ac4yProcess {

    @Override
    public Object process(Object input) throws Ac4yException {
        try {
            MyData data = (MyData) input;
            // transzformáció
            return transformedData;
        } catch (Exception e) {
            throw new Ac4yException("Transformation failed: " + e.getMessage());
        }
    }
}
```

### 2. Context-aware Process

```java
public class ContextAwareProcess extends Ac4yProcess {

    private Ac4yProcessContext context;

    @Override
    public Object process(Object input) throws Ac4yException {
        HttpServletRequest request = (HttpServletRequest) context.getExternal();
        Map<String, Object> cache = (Map) context.getInternal();

        // feldolgozás context alapján
        return result;
    }

    public void setContext(Ac4yProcessContext context) {
        this.context = context;
    }
}
```

### 3. Properties Betöltés

```java
try {
    ExternalPropertyHandler propHandler = new ExternalPropertyHandler();
    Properties config = propHandler.getPropertiesFromClassPath("myapp.properties");

    String apiUrl = config.getProperty("api.url");
    String apiKey = config.getProperty("api.key");

} catch (IOException | Ac4yException e) {
    ErrorHandler.addStack(e);
}
```

### 4. Exception Handling Pattern

```java
public class SafeProcessor extends Ac4yProcess {

    @Override
    public Object process(Object input) throws Ac4yException {
        try {
            // feldolgozás
            return result;

        } catch (SQLException e) {
            ErrorHandler.addStack(e);
            Ac4yException ae = new Ac4yException("Database error");
            ErrorHandler.addMessage(ae.getStackTraceAsString(e));
            throw ae;

        } catch (Exception e) {
            ErrorHandler.addStack(e);
            throw new Ac4yException("Processing failed: " + e.getMessage());
        }
    }
}
```

## AI Agent Használati Útmutató

### Gyors Döntési Fa

**Kérdés:** Mit szeretnél csinálni?

1. **Adatfeldolgozás** → `Ac4yProcess`
   - Saját feldolgozó logika? → Származtass Ac4yProcess-ből
   - Context kell? → Használj `Ac4yProcessContext`-et

2. **Hibakezelés** → `Ac4yException`, `ErrorHandler`
   - Saját exception dobása? → `throw new Ac4yException(msg)`
   - Log hiba? → `ErrorHandler.addStack(e)`
   - Stack trace string? → `exception.getStackTraceAsString(e)`

3. **Konfiguráció** → `ExternalPropertyHandler`
   - Properties betöltés? → `getPropertiesFromClassPath()`

4. **HTTP** → `Ac4yHttpService`
   - REST API hívás? → Használd Ac4yHttpService-t

### Token-hatékony Tudás

**Mit tartalmaz:**
- Process template
- Exception handling
- Properties loading
- HTTP service

**Mit NEM tartalmaz:**
- Database kapcsolat (→ ac4y-database)
- Connection pool (→ ac4y-connection-pool)
- Context factory (→ ac4y-context)

**Függőségek:**
- ac4y-utility (1.0.0)

**Bemeneti kivételek:** ClassNotFoundException, SQLException, IOException, ParseException
**Kimeneti kivétel:** Ac4yException

## Verzió Kompatibilitás

### v1.0.0
- Tartalmazta az utility osztályokat is
- Monolitikus struktúra

### v1.1.0 (jelenlegi)
- Utility osztályok kikerültek ac4y-utility modulba
- Függőség ac4y-utility 1.0.0-ra
- Tisztább felelősségi körök

## Build és Telepítés

```bash
# Build
mvn clean install

# Test
mvn test

# Deploy to GitHub Packages
mvn deploy
```

**GitHub Packages:**
```xml
<dependency>
    <groupId>ac4y</groupId>
    <artifactId>ac4y-base</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Kiterjesztési Pontok

### Custom Process Implementáció
```java
@XmlRootElement
public class MyBusinessLogic extends Ac4yProcess {
    @Override
    public Object process(Object input) throws Ac4yException {
        // üzleti logika
        return output;
    }
}
```

### Custom Exception Handling
```java
public class MyException extends Ac4yException {
    public MyException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    private int errorCode;
}
```

## Best Practices

1. **Mindig használj Ac4yException-t** Ac4y specifikus hibákhoz
2. **Context használata** komplex folyamatoknál ajánlott
3. **Properties fájlok** nevezzétek el beszédesen (module.properties)
4. **ErrorHandler** csak development módban, production-ben logger framework
5. **Process osztályok** legyenek single responsibility principle szerint
