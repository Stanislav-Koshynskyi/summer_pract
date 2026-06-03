# Tactical 2D Action-Stealth(поки без назви) — командний план розробки
> Документ є джерелом правди по проєкту на рівні поведінки систем, MVP-пріоритетів, Tiled-схеми, інтеграційних контрактів і acceptance criteria.
> Якщо є зміни, в першу чергу змінюється план, потім код
---

## 1. Як читати цей документ

### Обов'язковими є

- MVP-фази та порядок пріоритетів.
- Tiled schema: назви шарів, object types, required properties.
- Gameplay rules: смерть, рестарт, зір, звук, стрільба, двері, weapon swap, collision edge cases.
- System contracts: що має гарантувати core, client, content, level design.
- Розподіл відповідальності між розробниками.
- Acceptance criteria для тижнів і вертикальних зрізів.
- Edge-case rules які запобігають відомим багам.


Команда може змінювати внутрішню реалізацію якщо зберігаються зовнішня поведінка, контракти, Tiled-схема й acceptance criteria.

---

## 2. Загальний опис гри

Динамічний 2D top-down action-stealth шутер. Гравець проходить короткі рівні-будівлі використовуючи агресивний штурм або тихий стелс.

**Ключова ідея:** швидко зайшов, швидко прийняв рішення, швидко помер або зачистив, швидко перезапустив.

**Основні принципи:**
- one-hit-death для гравця
- короткі рівні, швидкий рестарт
- читабельна поведінка ворогів
- stealth і assault мають бути обидва життєздатними
- вороги небезпечні але не мають миттєвого perfect aim
- зір, звук і двері працюють передбачувано
- рівні підтримують кілька стилів проходження

Гра натхненна Hotline Miami за темпом і top-down камерою, але не копіює назви, персонажів, музику, UI, рівні або інші впізнавані елементи.

---

## 3. Технічний стек

- Мова: Java
- Фреймворк: LibGDX desktop
- Редактор рівнів: Tiled Map Editor
- Формат рівнів: .tmx
- Збереження: JSON
- Фізика: власна AABB-логіка, без Box2D
- Pathfinding: grid-based A* у core

**Рішення щодо core:** core не має залежати від LibGDX-графіки, ресурсів або `Gdx.*`. Заборонено використовувати `Texture`, `SpriteBatch`, `Sound`, `Music`, `Screen`, `Gdx.*`.

**Vec2/Rect рішення:** команда вирішує на старті і дотримується єдиного підходу. Змішування LibGDX `Vector2` і власних класів в одному модулі заборонено.

---

## 4. Модулі і залежності

```
core/     — gameplay rules, runtime state, systems, pure logic
client/   — LibGDX screens, rendering, input, audio, Tiled loading
content/  — weapons, enemies, goals, characters, campaign definitions, stateless behavior definitions
desktop/  — запуск гри, wiring dependencies
```

**Залежності (A → B означає "модуль A імпортує модуль B"):**
- `content` → `core`
- `client` → `core`
- `client` → `content`
- `desktop` → `client` (а також `core` та `content` для wiring)

**Розподіл логіки:**
- `core` містить увесь gameplay logic і runtime state.
- `content` містить дані та stateless gameplay definitions/behaviors. Content не мутує `LevelState` напряму.
- `desktop` — Composition Root.

**Контракт ініціалізації `GameController`:** приймає `WeaponRegistry`, `EnemyProfileRegistry` і `Map<AimBehaviorType, AimBehavior>`. Маппінг `AimBehaviorType` → реалізація `AimBehavior` відбувається виключно в `desktop`. Core знає тільки про інтерфейс `AimBehavior` і контрактний ключ `AimBehaviorType`; він не створює реалізації і не має `switch` по конкретних типах поведінки.

**MVP shortcut:** `AimBehaviorType` є спільним контрактним ключем між `core/content/desktop`. Це свідомий компроміс для швидкості розробки. Core може використовувати ключ для lookup у переданому map, але не створює конкретні `AimBehavior` і не містить content-specific wiring.

```
desktop створює:
  Map<AimBehaviorType, AimBehavior> aimBehaviors = {
    STANDARD → new StandardAim(),
    SNIPER   → new SniperAim(),
    MELEE    → new MeleeOnly()
  }
  → передає в GameController при ініціалізації
```

---

## 4а. Desktop wiring — Розробник Б

Desktop — Composition Root. Розробник Б збирає всі модулі разом при запуску і керує переходами між рівнями. Розробник В надає реєстри з даними; Б інжектить їх у `GameController`.

**Флоу запуску:**
```
1. В надає: WeaponRegistry, EnemyProfileRegistry, Map<AimBehaviorType, AimBehavior>
2. Б створює GameController(weaponRegistry, enemyProfileRegistry, aimBehaviors)
3. Б створює LibGDX Application і передає GameController в GameScreen
4. GameScreen стартує — game loop починається
```

**Флоу завантаження першого рівня:**
```
1. GameScreen отримує ім'я першого рівня з campaign config (або хардкод для MVP-0)
2. GameScreen викликає LevelTmxLoader.load("level_01.tmx") → LevelData
3. GameScreen викликає GameController.loadLevel(levelData)
4. GameController скидає LevelState і будує новий з LevelData
5. Рендеринг починається
```

**Флоу переходу між рівнями:**
```
1. GameScreen отримує LevelCompletedEvent з drainEvents()
2. GameScreen показує ResultScreen (outcome, статистика)
3. Гравець натискає "Next" або "Retry"
4. GameScreen викликає LevelTmxLoader.load("level_XX.tmx") → LevelData
5. GameScreen викликає GameController.loadLevel(levelData)
6. GameController скидає LevelState — fresh start
```

**Флоу смерті гравця:**
```
1. GameScreen отримує PlayerDiedEvent
2. GameScreen показує GameOverScreen або одразу перезапускає
3. GameScreen викликає GameController.loadLevel(currentLevelData)
   (той самий LevelData що зберігся при завантаженні — не перепарсити .tmx)
```

**Важливо:** `LevelData` зберігається в `GameScreen` після завантаження рівня. При рестарті `GameController.loadLevel()` викликається з тим самим об'єктом — `.tmx` не перепарситься кожен раз.

**`GameController` API для wiring:**
```java
GameController(WeaponRegistry, EnemyProfileRegistry, Map<AimBehaviorType, AimBehavior>)
void loadLevel(LevelData)
void update(float delta)
void movePlayer(float dx, float dy)
void aimPlayer(float worldX, float worldY)
void shoot()
void interact()
void setMovementMode(MovementMode)
GameStateView getStateView()
List<GameEvent> drainEvents()
```

---



### Ранній вертикальний зріз — перші 2–3 дні

Усі задачі відкладаються до появи цього прототипу:
- `test.tmx` з `PlayerSpawn`
- завантаження карти з Tiled
- collision зі стінами
- рендер карти
- рух гравця
- камера слідкує за гравцем

### MVP-0 — базовий vertical slice

Обов'язково: `test.tmx`; рух гравця; collision; камера; один тип ворога; hitscan-постріл; смерть ворога; `KillAllGoal`; рестарт; debug overlay (AABB і raycast).

**Acceptance criteria:**
- гравець рухається і не проходить крізь стіни
- hitscan-постріл вбиває ворога
- смерть гравця перезапускає рівень у початковий стан
- рестарт 100 разів поспіль не накопичує entities
- debug overlay показує AABB і raycast

### MVP-1 — action + базовий AI

Обов'язково: вороги з `PATROL`, `ATTACK`, `SEARCH`; FOV і line-of-sight; reaction delay; enemy hitscan не guaranteed hit; `SoundEventQueue`; вороги реагують на постріл через `SEARCH(soundPosition)`; двері `OPEN`/`CLOSED`/`OPENING`; pathfinding; pickup зброї; 1–2 playable рівні.

**Acceptance criteria:**
- ворог бачить гравця через FOV і не бачить крізь стіну
- ворог чує постріл і реагує
- ворог плавно рухається між pixel waypoints (не телепортується по тайлах)
- enemy hitscan не влучає 100%
- рівень проходиться без краш-багів

### MVP-2 — stealth loop

Обов'язково: `RUN`/`WALK`/`SNEAK`; footstep sounds; `LOW_OBSTACLE`; silent kill; aim memory; result screen; 2–3 рівні.

**Acceptance criteria:**
- `SNEAK` не генерує footstep звуки
- `LOW_OBSTACLE` ховає гравця у `SNEAK`
- silent kill тільки зі сліпої зони
- corner-peek не дає нескінченно стріляти
- result screen показує статистику і `LevelOutcome`

### MVP-3 — campaign loop

Обов'язково: menu; level select з `LevelOutcome`; JSON save; розблокування рівнів; retry/next flow.

**Acceptance criteria:**
- прогрес зберігається між сесіями
- наступний рівень розблоковується після завершення
- гравець може вибрати outcome для перепроходження
- всі campaign рівні проходяться від початку до кінця

### Stretch goals

- Магазин, гроші, unlock персонажів/екіпіровки
- 5–8 рівнів
- Реакція на тіла / `BODY_FOUND`
- Підмога через `SpawnPoint`
- Скло яке можна ламати
- Locked doors і ключі
- Другий персонаж і здібності
- `PiercingProjectile`, `SpreadProjectile`
- ЧБ/колір shader
- Вентиляція
- Path smoothing для плавніших маршрутів ворогів

---

## 6. Обов'язкові gameplay-контракти

### 6.1 Player death і restart

Гравець має 1 HP. Одне влучання або melee-hit вбиває.
Після смерті — fresh runtime state. Meta/campaign progress не змінюється до завершення рівня.

### 6.2 Координатна система і pathfinding

**Entity позиція — завжди в world pixels (float).**
- Hitbox — AABB симетрично навколо центру.
- При рендерингу: `drawX = position.x - sprite.width / 2`.
- Tile coordinate — `floor(centerX / tileSize)`, `floor(centerY / tileSize)`.

**Pathfinding координати:**
- A* граф будується в тайлових координатах (цілі числа) — простіше будувати і дебажити.
- Результат A* — список тайлових вузлів — одразу конвертується в список pixel waypoints: `pixelX = tileX * tileSize + tileSize / 2`.
- Ворог рухається плавно між pixel waypoints кожен кадр зі своєю швидкістю.
- Entity ніколи не телепортується між тайлами — позиція завжди інтерпольована в пікселях.
- A* перевіряє прохідність з урахуванням AABB entity: стіни/закриті двері вважаються розширеними на half-size entity, щоб маршрут не проходив через коридори де тіло реально застрягне.
- Для pathfinding двері у стані `CLOSED` вважаються traversable with high cost. Для runtime collision ті самі двері блокують рух, доки не перейдуть у `OPEN`.

**Collision** перевіряє всі тайли що перетинає AABB.
У MVP entities не блокують рух одне одного. Entity-vs-entity collision, pushing і anti-crowding — stretch/поліш після стабільного movement loop.

**LibGDX Y-вісь:** LibGDX малює знизу вгору, Tiled зберігає зверху вниз. `LevelTmxLoader` конвертує Y при завантаженні: `worldY = (mapHeightInTiles - tileY - 1) * tileSize`.

### 6.3 Hitscan combat

- Постріл — миттєвий raycast до aim point.
- Shooter виключається з власного raycast — не може вбити себе.
- Raycast збирає всі перетини (walls, closed doors, enemies), сортує за дистанцією.
- `DOOR_OPENING` блокує кулі — поки двері анімують відкриття вони непрозорі для пострілів.
- **Friendly fire дозволений:** вороги можуть влучати в інших ворогів.

### 6.4 Enemy HP

- Гравець завжди 1 HP. Вороги мають `hp > 0` з `EnemyProfile`.
- Підтримка `hp > 1` є частиною базового дизайну core.

### 6.5 Enemy hitscan fairness

- Має `reactionTime` перед першим пострілом.
- Не може стріляти без line-of-sight.
- Використовує `lastAimPoint` зафіксований в момент `shotCommitDuration` назад.
- Промах моделюється через committed aim point + distance/movement error: якщо гравець рухається після commit або ворог стріляє здалеку, шанс/кут помилки збільшується.
- Близька нерухома ціль після reaction delay має бути дуже небезпечною; далека рухома ціль не повинна давати guaranteed hit.

**`reactionTime` при повторному виявленні — залежить від стану:**
- `PATROL` → повний `reactionTime`
- `SEARCH` або `INVESTIGATE` → скорочений (ворог вже в режимі готовності)
- `aimMemoryTimer` ще активний → без затримки (ворог ще пам'ятає гравця)
- Повернувся в `PATROL` після закінчення `aimMemoryTimer` → повний `reactionTime` знову

Орієнтовні значення: `reactionTime` 0.30–0.45s, `shotCommitDuration` 0.08–0.15s, `aimMemoryDuration` 0.20–0.30s, `weaponCooldown` 0.60–0.90s.

### 6.6 Aim memory і anti-corner abuse

Втративши LoS ворог у `ATTACK` не переходить миттєво в `SEARCH`.
Поки `aimMemoryTimer > 0` тримає напрям на `lastKnownPlayerPosition` (без стрільби).
Якщо гравець з'явився до завершення timer — наведення продовжується.

### 6.7 Anti-strafing

Ворог стріляє тільки якщо гравець в attack cone. `aimTrackingSpeed` більша за швидкість бігу але не миттєва.

### 6.8 Weapon система і ammo

- `maxAmmo = -1` → infinite. Melee завжди `-1`.
- `CharacterProfile` має `defaultWeapon` (infinite) як fallback.
- Коли `currentWeapon.ammo == 0` → переключається на `defaultWeapon`.
- Порожня зброя дропається як `WeaponPickup` з візуальною відмінністю (наприклад інший відтінок або іконка). Не може бути підібрана.
- Перезарядки немає в MVP. UI показує патрони тільки якщо `maxAmmo != -1`.

### 6.9 Weapon swap

- Підбір через `interact`. Стара зброя дропається зі scatter-зсувом 10–15 px.
- Вибирається найближчий pickup до центру гравця.

### 6.10 Vision і LOW_OBSTACLE

- Видимість рахується per ворог. Глобального `isHidden` немає.
- `WALL` і `CLOSED` двері завжди блокують зір.
- `LOW_OBSTACLE` блокує зір тільки якщо гравець у `SNEAK`.
- Якщо ворог ближче ніж `closeRevealRadius` (32–64 px) — бачить гравця за `LOW_OBSTACLE`.
- `LOW_OBSTACLE` не реалізується як простий `Tile.blocksVision() == true/false`. Це динамічна перевірка у `VisionSystem`, бо результат залежить від target movement mode і дистанції до ворога.

### 6.11 Sound events

Звук поширюється колом: `Math.hypot(dx, dy) < radius`.

**Поглинання:** алгоритм Брезенхема між джерелом і ворогом. Якщо лінія перетинає `WALL`-тайл або `DOOR_CLOSED` — ефективний радіус ділиться на 3.

`SNEAK` беззвучний. Вороги в `ATTACK` ігнорують `SoundEvent`. `SoundEvent` живе один кадр.

| SoundType | Радіус | Примітки |
|---|---|---|
| `GUNSHOT` | великий | будь-який постріл |
| `SUPPRESSED_SHOT` | малий | зброя з глушником |
| `FOOTSTEP_RUN` | середній | режим RUN |
| `FOOTSTEP_WALK` | мінімальний | режим WALK |
| `BODY_FALL` | малий | тихе вбивство |

### 6.12 Вороги та двері

- Вороги відчиняють тільки двері у стані `CLOSED`. `LOCKED` вороги не можуть відчинити.
- A* будує маршрут через `DOOR_CLOSED` з high cost → ворог підходить і викликає `requestDoorOpen`.
- `requestDoorOpen`: якщо двері вже `OPENING` або `OPEN` — виклик ігнорується.
- Канонічний enum станів дверей: `OPEN`, `CLOSED`, `OPENING`, `LOCKED`. У MVP використовується `OPEN/CLOSED/OPENING`; `LOCKED` залишається для stretch.

### 6.13 LevelOutcome

Outcome визначається не самим фактом пострілу, а рівнем реакції ворогів.

Пріоритети:
- `ASSAULT`: багато ворогів входили в `ATTACK`.
- `FULL_STEALTH`: `uniqueEnemiesAlerted == 0` — жоден ворог не входив у `ATTACK`, `SEARCH`, `INVESTIGATE` або `SUSPICIOUS`.
- `MIXED`: все інше — частина ворогів щось помітила, але рівень не перетворився на повний штурм.

Для MVP поріг `ASSAULT` можна захардкодити: наприклад, `uniqueEnemiesEnteredAttack >= 3` або `>= 50%` від живих ворогів на старті рівня. Конкретне число налаштовується після playtest.

`gunshotFired` і `suppressedShotFired` лишаються raw stats, але самі по собі не визначають `LevelOutcome`.

`LevelTmxLoader` завантажує `level_XX_stealth.tmx` або `level_XX_assault.tmx` якщо існують, інакше `level_XX.tmx`. Різні варіанти карт — опція для левел-дизайнера, не обов'язок.
У campaign/level select outcome-варіанти карти можна вибирати тільки після першого завершення рівня. До першого проходження запускається дефолтна карта.

`kill_target` — optional/stretch goal. Loader не зобов'язаний підтримувати його в MVP. Якщо знадобиться для демо, можна захардкодити target enemy id у campaign/content config і перевіряти через `GameStateView`, чи живий ворог з цим id.

### 6.14 Knockback і Collision

Knockback — `knockbackVelocity` що згасає (не телепорт).
Collision: moveX → відкотити X якщо collision → moveY → відкотити Y якщо collision.

### 6.15 Трупи

Залишаються в `LevelState` з `alive = false`, `bodyInvestigated = false`.
У MVP трупи не пропадають до рестарту рівня і рендеряться як corpses, але AI-реакція на них не є першочерговою.
`bodyInvestigated` і реакція ворогів на тіла (`BODY_FOUND`) — stretch/MVP-3+ polish. При рестарті трупи видаляються разом із fresh runtime state.

### 6.16 Interact priority

```
1. Silent kill — ворог поза FOV-конусом на відстані interact-радіусу.
   Якщо кілька ворогів в зоні — найближчий або той на кого дивиться гравець.
   Дозволено тільки якщо ворог зараз не бачить гравця. Ворог у `ATTACK` вважається таким що бачить гравця, тому silent kill проти нього недоступний.
   Між гравцем і ворогом не має бути `WALL` або `DOOR_CLOSED/LOCKED`.
   closeRevealRadius не впливає на доступність silent kill.
2. Відкрити двері
3. Підібрати зброю
4. Зайти в укриття — optional MVP-2+. У MVP-0/1 cover не є окремою interact-дією; `LOW_OBSTACLE` працює через `SNEAK`.
```

---

## 7. AI — Finite State Machine

### Стани

| Стан | Поведінка |
|---|---|
| `PATROL` | рух по waypoints. Один waypoint — стоїть на місці і обертається |
| `ATTACK` | переслідує і атакує з reaction delay і aim memory |
| `SEARCH` | детально нижче |

MVP-2+ додає: `SUSPICIOUS` і `INVESTIGATE`.

**MVP-1 rule for sound:** якщо ворог чує звук, але окремого `INVESTIGATE` ще немає, він переходить у `SEARCH`, а `lastKnownPlayerPosition` тимчасово ставиться в позицію звуку. Це дає реакцію на постріл без додаткового стану.

### SEARCH — повний контракт

- Ворог рухається до `lastKnownPlayerPosition`.
- Якщо дорогою або після прибуття бачить гравця → `ATTACK`.
- Якщо досягнув точки і не знайшов → короткий огляд → `PATROL`.
- Якщо pathfinding до `lastKnownPlayerPosition` неможливий (точка в стіні тощо) → одразу `PATROL`.
- **Втрата LoS ніколи не повертає ворога миттєво в `PATROL`.** Завжди спочатку `SEARCH`.

### Пріоритети переходів

```
1. Бачить гравця → ATTACK (або SUSPICIOUS в MVP-2)
2. Почув звук → SEARCH(soundPosition) у MVP-1, INVESTIGATE у MVP-2+
3. Виконати поточний стан
```

### Кут зору

- `patrolFovAngle` — вужчий під час руху
- `alertFovAngle` — ширший під час стояння
- `facingAngle` — окреме поле незалежне від напряму руху

---

## 8. Типи тайлів

```java
enum VisionBlocker {
    NONE,
    SOLID,
    LOW_OBSTACLE
}

interface Tile {
    VisionBlocker visionBlocker();
    boolean canPass(Entity entity);
    boolean blocksProjectile();
    float soundAttenuationFactor(); // 1.0 = без поглинання, < 1.0 = поглинає
}
```

| Тип | Рух | Зір | Куля | Звук |
|---|---|---|---|---|
| `FLOOR` | всі | ні | ні | 1.0 |
| `WALL` | ніхто | так | так | ÷3 |
| `LOW_OBSTACLE` | всі | тільки якщо target у SNEAK | ні | 1.0 |
| `DOOR_OPEN` | всі | ні | ні | 1.0 |
| `DOOR_CLOSED` | ніхто | так | так | ÷3 |
| `DOOR_OPENING` | ніхто (фізика) | ні | так | 1.0 |
| `VENT` | тільки `canUseVents && SNEAK` | так | так | stretch |

`DOOR_OPENING` — проміжний стан анімації фіксованої тривалості. Після початку гарантовано переходить у `OPEN`. Повернення `OPENING → CLOSED` не існує.
`DOOR_OPENING` навмисно прозорі для зору, але блокують кулі: гравець/ворог може побачити рух за дверима під час відкриття, але не прострелити двері до завершення анімації.

---

## 9. Tiled Object Schema

Узгоджується Розробником Г і Б в перші 1–2 дні. Core не знає про Tiled.

**Шари:** `floor`, `walls`, `low_obstacles`, `doors`, `objects`.

**Як виставити ціль рівня в Tiled:**
Відкрити Map → Map Properties (меню Map або панель Properties). Додати custom property:
- `goalType` (string) — обов'язково для кожного рівня. Значення: `"kill_all"`, `"escape"`, `"kill_target"`
- `targetEnemyId` (string) — тільки якщо `goalType == "kill_target"`; має збігатись з `enemyId` одного з `EnemySpawn` об'єктів

Без `goalType` рівень не пройде валідацію при завантаженні.

**Object types:**
- **PlayerSpawn**: `x`, `y`
- **EnemySpawn**: `enemyTypeId`, `weaponId`, `patrolPathId`, `facingAngle`, optional `enemyId` (string, для `kill_target` goal)
- **Waypoint**: `pathId`, `order`
- **WeaponPickup**: `weaponId`
- **Door**: `doorId`, `initialState` (`"open"` або `"closed"` у MVP), optional `orientation`
- **LowObstacle**: `lowObstacleId`
- **Exit**: `exitId`

**Як виставити властивість об'єкту в Tiled:**
Виділити об'єкт на карті → панель Properties → додати custom property з потрібним ім'ям і значенням. Наприклад для `EnemySpawn`: `enemyTypeId = "guard"`, `weaponId = "pistol"`, `patrolPathId = "path_01"`, `facingAngle = 270`.

**LevelData контракт** (що `LevelTmxLoader` передає в `GameController.loadLevel()`):
```
LevelData:
  WorldGeometry        — тайлова сітка з типами тайлів
  PlayerSpawnData      — позиція в world pixels
  List<EnemySpawnData> — позиція, enemyTypeId, weaponId, patrolPathId, facingAngle, enemyId
  List<DoorData>       — doorId, world AABB, initialState, список blocked tiles
  List<WaypointData>   — pathId, order, позиція в world pixels
  List<WeaponPickupData> — weaponId, позиція в world pixels
  List<ExitData>       — exitId, world AABB
  GoalType             — kill_all / escape / kill_target
  String targetEnemyId — nullable
```
Всі позиції в world pixels з LibGDX системою координат (Y знизу вгору). `LevelTmxLoader` відповідає за конвертацію: `worldY = (mapHeightInTiles - tileY - 1) * tileSize`. Після цього core і client більше не думають про Tiled-координати.

**Door geometry:** loader перетворює двері у стабільний runtime `Door` з world AABB, `DoorState` і списком blocked tiles для pathfinding/collision.

---

## 10. Level validation

При завантаженні `LevelTmxLoader` перевіряє:
- наявність `goalType` в map properties
- наявність одного `PlayerSpawn`
- валідні `enemyTypeId` і `weaponId` в реєстрах
- всі `patrolPathId` мають waypoints
- наявність `Exit` якщо `goalType == "escape"`
- наявність `targetEnemyId` і відповідного `EnemySpawn.enemyId` якщо `goalType == "kill_target"` і loader підтримує цей goal
- спавни не в стінах
- двері мають валідний `initialState`, AABB і хоча б один blocked tile
- `WeaponPickup` не в дверних отворах

---

## 11. Update loop

```
GameController.update(delta):
1.  Зчитати pending input commands
2.  Оновити player movement intent
3.  FootstepEmitter — згенерувати footstep SoundEvents для RUN/WALK
4.  HearingSystem — обробити SoundEventQueue
5.  VisionSystem — оновити видимість
6.  EnemyAI — оновити FSM, path target, movement intent і fire requests (тільки живі вороги; position напряму не мутувати)
7.  CollisionSystem — застосувати player/enemy movement і knockback
8a. InteractionSystem — input interactions: door interact, pickup, silent kill
    (перевіряє фінальну позицію гравця після collision — без лагу)
8b. DoorSystem — requestDoorOpen від ворогів; таймери дверей (OPENING → OPEN)
9.  WeaponSystem — cooldowns, hitscan, apply damage
10. Перевірити deaths → перемістити в corpses список
11. Оновити LevelStats і перевірити LevelGoal
12. Згенерувати GameEvents
13. Очистити one-frame events
```

**Інваріант:** `HearingSystem`, `VisionSystem`, `EnemyAI` ніколи не отримують мертві entities — вони видаляються на кроці 10 до наступного кадру.
**`GamePhase` contract:** якщо `PLAYER_DEAD` або `LEVEL_COMPLETED` — кроки 3–10 пропускаються.
**Movement contract:** системи AI/interaction задають intent або request. Реальне зміщення entity проходить через `CollisionSystem` на кроці 7, щоб жодна система не обходила collision.
**Interaction timing:** `InteractionSystem` на кроці 8a перевіряє фінальну позицію гравця після collision — pickup і silent kill реагують на реальне положення цього кадру.

---

## 12. GameStateView

Read-only wrapper над живими об'єктами core. Безпечний у single-threaded game loop.

- `getEnemyCount()` і `getEnemy(int index)` замість прямого списку.
- `isEnemyAlive(String enemyId)` — для optional/stretch `KillTargetGoal`.
- `isPlayerAtExit()` — для `EscapeGoal`.
- `getPlayerPosition()` — для рендерингу і логіки.
- `getStats()` або окремі read-only getters для `LevelStats` — для `LevelOutcome`.
- Каст з view-типів назад до core-типів заборонений.

---

## 12а. LevelStats

Сирі факти в core:
- `uniqueEnemiesAlerted: int` або set enemy ids для будь-якого `ATTACK`/`SEARCH`/`INVESTIGATE`/`SUSPICIOUS`
- `uniqueEnemiesEnteredAttack: int` або set enemy ids
- `uniqueEnemiesEnteredSearchOrInvestigate: int` або set enemy ids
- `gunshotFired: boolean`, `suppressedShotFired: boolean`
- `killCount`, `silentKillCount`, `alertCount`, `elapsedTime`

`LevelOutcome` визначається у content через `GameStateView` і read-only `LevelStats`.

---

## 12б. GameEvents

`GameEvents` — черга одноразових подій які core генерує на кроці 12 update loop. `AudioManager` і рендерер в client підписуються на одну й ту саму чергу незалежно — одна подія може тригерити і звук і анімацію одночасно. Події живуть один кадр і очищаються на кроці 13.

**Мінімальний список для MVP:**

| Подія                   | Поля                                           | Використання |
|-------------------------|------------------------------------------------|---|
| `ShotFiredEvent`        | `from`, `hitPoint`, `weaponId`, `isSuppressed` | лінія пострілу (рендер кілька кадрів) + звук пострілу |
| `MeleeAttackEvent`      | `attackerX`, `attackerY`, `directionDeg`, `weaponId`, `hitPoints` | анімація ближнього бою + звук |
| `EnemyDiedEvent`        | `position`, `enemyId`                          | анімація смерті + звук смерті |
| `SilentKillEvent`       | `position`                                     | анімація тихого вбивства + тихий звук |
| `PlayerDiedEvent`       | `position`                                     | death flash + звук + тригер GameOverScreen |
| `DoorStateChangedEvent` | `doorId`, `newState`                           | анімація дверей + звук відкриття/закриття |
| `FootstepEvent`         | `position`, `movementMode`, `isPlayer`         | звук кроку + опціонально пилова анімація |
| `WeaponPickedUpEvent`   | `weaponId`, `position`                         | звук підбору + flash на HUD |
| `WeaponDroppedEvent`    | `weaponId`, `position`, `isEmpty`              | звук дропу; `isEmpty` для візуальної відмінності |
| `EnemyAlertedEvent`     | `enemyId`, `position`, `newState`              | `"!"` або `"?"` над ворогом + alert звук |
| `LevelCompletedEvent`   | `outcome`                                      | тригер ResultScreen |

**Правило розподілу подій vs GameStateView:**
- `GameEvent` — для одноразових ефектів що живуть кілька кадрів (звук, спалах, лінія пострілу, іконка над головою)
- `GameStateView` — для постійного стану що рендериться кожен кадр (позиції, HP, ammo, стани дверей)

Наприклад `ammo` читається через `GameStateView` кожен кадр — окремий `AmmoChangedEvent` не потрібен.

---



### 13.1 Core — Розробник А

**Тиждень 1:**
- Структури: `Entity`, `Player`, `Enemy`, `LevelState`, `GamePhase`
- `WorldGeometry`, `CollisionSystem` (AABB split-axis, multi-tile)
- Рух гравця, hitscan raycast (shooter excluded)
- `GameController`, `drainEvents()`
- `WeaponSystem` з `WeaponBehavior` інтерфейсом

**WeaponBehavior abstraction:**
```java
interface WeaponBehavior {
    void fire(...);
}
// Реалізації: HitscanWeaponBehavior, ShotgunWeaponBehavior, MeleeWeaponBehavior
```
Новий тип зброї — нова реалізація без зміни існуючого коду.

`WeaponBehavior` — stateless logic. Ammo, cooldown, owner і runtime weapon state живуть у core state/weapon instance.

Реалізації `WeaponBehavior` можуть лежати в content як MVP-компроміс, але вони не мають прямого доступу до `LevelState` і не мутують його напряму. Єдиний дозволений шлях зміни гри — через core-owned `WeaponFireContext`/API, який передає `WeaponSystem` (`spawnRaycast`, `applyDamage`, `emitSound`, `spawnPickup` тощо). Якщо реалізація потребує прямого доступу до runtime state — її треба перенести в core.

**Тиждень 2:**
- `EnemyAI` — один клас з приватними методами
- `AimBehavior` інтерфейс: `StandardAim`, `SniperAim`, `MeleeOnly`
- `VisionSystem`, reaction delay з урахуванням стану ворога
- A* pathfinding → конвертація в pixel waypoints → steering до наступної точки

**AI ownership:** Розробник А є owner `EnemyAI`, FSM transitions, pathfinding, vision/reaction timing і aim memory. Інші розробники не змінюють внутрішню логіку `EnemyAI` напряму; вони інтегруються через core API/events/stimuli.

**Тиждень 3:**
- `MovementMode` (`RUN`/`WALK`/`SNEAK`), `closeRevealRadius`
- Aim memory і anti-corner
- `GameStateView` read-only wrapper з `isPlayerAtExit()`

---

### 13.2 Client — Розробник Б

**Тиждень 1 (критичний):**
- LibGDX setup, Viewport, camera
- Узгодити Tiled schema з Г (день 1–2)
- `LevelTmxLoader` з Y-вісь конвертацією
- `GameScreen`, debug overlay F1
- Desktop wiring (розділ 4а): отримує реєстри від В, створює `GameController`, збирає `Application`

**Тиждень 2:**
- Рендеринг дверей з анімацією `DOOR_OPENING`
- Debug: FOV, sound radius, pathfinding path (pixel waypoints), AIState
- Audio mapping з `GameEvents`

**Тиждень 3:**
- `LOW_OBSTACLE` фідбек в `SNEAK`
- `GameOverScreen`, `ResultScreen`, screen transitions
- Audio polish

**Render layers:**
`floor → walls → low_obstacle → doors → pickups → corpses → enemies → player → effects → HUD → debug`

**Debug overlay (F1):** AABB, AIState, `facingAngle`, FOV cone, `lastKnownPlayerPosition`, sound radius, pathfinding pixel waypoints, door states, tile coordinates.

---

### 13.3 Content + Core systems — Розробник В

**Boundary with AI:** Розробник В не відповідає за `EnemyAI` internals, FSM, pathfinding або vision logic. Його системи можуть створювати stimuli/events для AI, але не мутують AI state напряму. `EnemyAI` читає ці stimuli у своєму update.

**Що А має передати В як API/stubs:**
- `LevelState` read/write API для pickup, doors, damage через дозволені methods/context
- `GameEvents` / `SoundEventQueue`
- `WeaponFireContext`
- `EnemyStimulus` або еквівалентний спосіб повідомити AI: `heardSound(enemyId, position, soundType)`
- `GameStateView` для goals/outcome
- базові ids для entities, doors, pickups, weapons

**Мінімальні контракти між А і В:**

Це behavioral contracts, не фінальні Java signatures.

`HearingSystem`:
- input: active `SoundEventQueue`, world geometry/read-only collision data, список живих ворогів
- output: AI stimuli/events типу "enemy heard sound at position"
- forbidden: напряму міняти `EnemyAI` state, path, target або FSM

`FootstepEmitter`:
- input: positions, movement modes, movement timers, `delta`
- output: додає `FOOTSTEP_RUN` / `FOOTSTEP_WALK` у `SoundEventQueue`
- forbidden: напряму викликати AI або міняти outcome/stats

`InteractionSystem`:
- input: player interact command, player position/facing, nearby interactables
- output: requests/effects через core-approved operations: open door, pickup weapon, silent kill, emit event
- forbidden: обходити collision або напряму редагувати приватні списки entities/pickups/doors

`EnemyAI` integration:
- AI читає stimuli у своєму update
- В не викликає `enemy.setState(...)` напряму
- якщо потрібен новий тип stimuli, А і В узгоджують його як частину core contract

**Тиждень 1 (content):**
- `WeaponDefinition`, `EnemyProfile` з `aimBehaviorType: enum`
- `KillAllGoal`, `defaultWeapon` fallback
- Надає Б готові реєстри для desktop wiring: `WeaponRegistry`, `EnemyProfileRegistry`, `Map<AimBehaviorType, AimBehavior>` — Б інжектить їх у `GameController`
- Weapon behavior definitions для різної логіки зброї (`hitscan`, `melee`, пізніше `spread`), без власного mutable runtime state

**Тиждень 1–2 (core, узгоджено з А):**
- `InteractionSystem` — двері, підбір зброї, silent kill (найближчий або за напрямом погляду)
- `HearingSystem` — `SoundEventQueue`, Брезенхем з урахуванням `WALL` і `DOOR_CLOSED`; результатом є AI stimulus/event, а не прямий перехід стану ворога
- `FootstepEmitter` — таймер per `MovementMode`

**Тиждень 2 (content):**
- `EscapeGoal` — використовує `GameStateView.isPlayerAtExit()`
- Баланс

**Тиждень 3:**
- `LevelStats`, `LevelOutcome` визначення
- JSON save (`completedLevels`, `bestOutcome`)
- `MenuScreen`, `LevelSelectScreen`
- Campaign progression

---

### 13.4 Level Design — Розробник Г

**Тиждень 1:**
- Вивчити Tiled
- Узгодити і задокументувати schema з Б (день 1–2) — включно з `goalType` property
- `test.tmx` для MVP-0
- Debug/test maps для перевірки систем: collision room, door room, FOV/LoS room, sound reaction room, patrol/pathfinding room

**Тиждень 2:**
- 1–2 playable рівні

**Тиждень 3:**
- 2–3 campaign рівні
- Кожен рівень підтримує assault і stealth
- Баланс складності

**Принципи:** до стабільного MVP-1 не гнатись за кількістю рівнів. Перший пріоритет — test maps і один якісний playable vertical slice. Безпечна перша секунда; мінімум одна assault і одна stealth опція; `WeaponPickup` не в дверних отворах; рівномірний patrol coverage.

---

### 13.5 Art / Audio / Media — Дизайнер

**Пріоритет:** дизайнер у першу чергу фокусується на assets які впливають на читабельність gameplay: персонажі, вороги, зброя, pickups, двері, укриття/`LOW_OBSTACLE`, floor/wall tiles і дизайн локацій. UI polish, музика, відео і декоративні ефекти йдуть після того, як основні gameplay assets працюють у грі.

**Тиждень 1:**
- Placeholder спрайти (потрібні Б одразу)
- Узгодити візуальний стиль
- UI мокапи всіх екранів
- Референси для звуків
- Перший набір gameplay assets: player, enemy, базова зброя, weapon pickup, floor/wall tiles, door, low obstacle

**Тиждень 2:**
- Фінальні спрайти: гравець, вороги, зброя (порожня зброя візуально відрізняється), укриття, двері
- `LOW_OBSTACLE` — нижчий силует щоб відрізнятись від стіни
- Дизайн локацій: читабельні floor/wall/door/cover tiles, щоб гравець швидко відрізняв прохід, стіну, двері, pickup і укриття
- Звуки: постріли (гучний/тихий), кроки (3 режими), двері, alert, смерть
- Музика (OpenGameArt, freesound.org)
- Початок звіту

**Тиждень 3:**
- UI polish
- Звіт
- Відеоролик

---

## 14. Stretch goal: ЧБ/колір shader

Fragment Shader знижує `saturation` до нуля. UI завжди кольоровий. Кров червона.

- ЧБ → колір: `onAlertRaised()`, 0.3–0.5s
- Колір → ЧБ: `onAlertCleared()`, 1–2s

Не братись до shader до стабільного MVP-3.

---

## 15. Відкриті питання

| Питання | Статус |
|---|---|
| Vec2/Rect — LibGDX чи власні | Команда вирішує на старті, єдиний підхід |