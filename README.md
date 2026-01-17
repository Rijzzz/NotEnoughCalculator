# 🧮Not Enough Calculator

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/notenoughcalculator?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/mod/notenoughcalculator)
[![CurseForge](https://img.shields.io/curseforge/dt/1431725?logo=curseforge&label=CurseForge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/notenoughcalculator)
[![GitHub Release](https://img.shields.io/github/v/release/Rijzzz/NotEnoughCalculator?logo=github&label=Latest%20Release)](https://github.com/Rijzzz/NotEnoughCalculator/releases)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](LICENSE)

**A powerful calculator mod that brings NEU-style calculator functionality to Roughly Enough Items (REI).**

> Inspired by [NotEnoughUpdates's](https://modrinth.com/mod/notenoughupdates) calculator, recreated for newer Minecraft versions where NEU isn't available.

![Showcase](https://cdn.modrinth.com/data/cached_images/45906956cc96974985abed61ad9c769495409b4e_0.webp)

![Showcase 2](https://cdn.modrinth.com/data/cached_images/dcf5816503a635c49cee07b06fb5abda9dd8286a_0.webp)

---

## What does this mod do?

Type calculations directly in the REI search bar and get instant results! Perfect for Hypixel Skyblock players who miss the NEU calculator.

**Example:** Type `100m - 50m` in the REI search → See `= 50,000,000` instantly

---

## Key Features

<details>
<summary><strong>Basic Math</strong></summary>

- Addition, subtraction, multiplication, division (`+`, `-`, `*`, `/`)
- Exponents and modulo (`^`, `%`)
- Parentheses for complex expressions
- Functions: `sqrt()`, `abs()`, `floor()`, `ceil()`, `round()`

</details>

<details>
<summary><strong>Hypixel Skyblock Units</strong></summary>

- Currency: `k`, `m`, `b`, `t` (thousand, million, billion, trillion)
- Items: `s` (stack = 64), `e` (enchanted = 160)
- Storage: `h` (shulker = 1,728), `sc` (small chest = 1,728), `dc` (double chest = 3,456), `eb` (ender chest = 2,880)

</details>

<details>
<summary><strong>Variables</strong></summary>

- `ans` – Automatically stores last calculation result
- Custom variables with `/calcset` command
- Chain calculations easily with `ans`

</details>

<details>
<summary><strong>History & Shortcuts</strong></summary>

- Press `Ctrl+Z` in REI search to recall previous calculations
- Press `Ctrl+Y` to redo/go forward in history
- View full history with `/calchist` (shows last 10 entries)
- Session-based: History clears automatically when you leave a world/server

</details>

<details>
<summary><strong>Customization</strong></summary>

- Configurable decimal precision (default: 10 digits)
- Toggle features on/off via config file
- Comma formatting for large numbers

</details>

---

**Dependencies (Required):**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Roughly Enough Items (REI)](https://modrinth.com/mod/rei) 

---

**How to Install:**
1. Download the required mods and place them into your Minecraft `mods/` folder:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Roughly Enough Items (REI)](https://modrinth.com/mod/rei)
   - **Not Enough Calculator** (this mod)
2. Launch Minecraft using the Fabric loader
3. Press your inventory key to open REI
4. Start typing calculations directly into the REI search bar

---

<details>
<summary>Examples</summary>

### Auction House & Trading
```
100m - 75m = 25,000,000          (profit calculation)
100m * 0.05 = 5,000,000           (5% auction tax)
50m * 1.1 = 55,000,000            (10% markup)
(100m - 60m) * 0.95 = 38,000,000 (profit after tax)
1b / 100 = 10,000,000             (price per item)
250m + 150m + 75m = 475,000,000  (total spending)
```

### Inventory & Storage Management
```
640 / 64 = 10                     (stacks needed)
5s * 2 = 640                      (10 stacks)
3h = 5,184                        (3 shulkers total items)
2dc + 5sc = 15,552                (storage capacity)
10000 / 64 = 156.25               (156 stacks + 16 items)
27s = 1,728                       (full shulker)
```

### Crafting & Resources
```
160 * 5 = 800                     (5 enchanted = 800 items)
5e * 3 = 2,400                    (crafting cost)
64 * 27 = 1,728                   (shulker capacity)
1728 / 160 = 10.8                 (enchanted items per shulker)
9 * 160 = 1,440                   (enchanted block crafting)
```

### Mining & Farming
```
500 + 300 + 200 = 1,000           (total ores mined)
1000 / 3 = 333.33                 (average per hour)
dc / 333 = 10.38                  (hours to fill double chest)
64 * 100 = 6,400                  (100 stacks)
6400 / 160 = 40                   (enchanted items)
```

### Money & Profits
```
1000m - 800m = 200,000,000        (200m profit)
500m * 0.20 = 100,000,000         (20% profit margin)
(1000m - 500m) / 500m = 1         (100% ROI)
100m * 2.5 = 250,000,000          (2.5x flip)
50m * 30 = 1,500,000,000          (bulk buying)
```

### Advanced Math
```
sqrt(144) = 12                    (square root)
2^10 = 1,024                      (exponents)
100 % 7 = 2                       (modulo)
abs(-50) = 50                     (absolute value)
floor(3.9) = 3                    (round down)
ceil(3.1) = 4                     (round up)
round(3.5) = 4                    (round nearest)
```

### Compound Calculations
```
100m * (1.05 ^ 10) = 162,889,462.68     (10 weeks compound)
(50 + 25) * 1000 = 75,000               (grouped operations)
(sqrt(2500) + abs(-100)) / 2 = 75       (complex formula)
((100 + 50) * 2) - 50 = 250             (nested parentheses)
```

### Price Per Item
```
1b / 1000 = 1,000,000             (price per unit)
500m / 64 = 7,812,500             (price per stack)
100m / 160 = 625,000              (price per enchanted)
50m / 27 = 1,851,851.85           (price per slot)
```

### Percentage Calculations
```
100m * 0.85 = 85,000,000          (15% discount)
500m * 1.25 = 625,000,000         (25% increase)
(200m - 150m) / 150m = 0.3333     (33% profit margin)
1b * 0.01 = 10,000,000            (1% tax)
```

### Material Conversions
```
160 * 64 = 10,240                 (1 stack enchanted blocks)
9 * 9 * 160 = 12,960              (1 enchanted block breakdown)
64 * 64 = 4,096                   (double compressed)
1728 / 64 = 27                    (stacks per shulker)
```

### Business & Investment
```
1000m * 0.02 = 20,000,000         (2% daily interest)
20m * 365 = 7,300,000,000         (yearly earnings)
(1000m + 200m) / 2 = 600,000,000  (average investment)
500m * 1.5 = 750,000,000          (50% growth)
```

### Combat & Stats
```
(100 + 50 + 25) / 3 = 58.33       (average damage)
sqrt(10000) = 100                 (damage calculation)
200 * 1.5 = 300                   (crit damage)
(500 - 100) * 1.2 = 480           (defense reduction)
```

### Bulk Operations
```
50 * 100m = 5,000,000,000         (buying 50 items)
1000 * 500k = 500,000,000         (bulk crafting cost)
64 * 15625 = 1,000,000            (precise calculations)
27 * 64 * 100 = 172,800           (mass storage)
```

### Using Variables
```
/calcset buy 50m
/calcset sell 75m
$sell - $buy = 25,000,000         (profit using variables)

/calcset stacks 10
$stacks * 64 = 640                (items from stacks)

100 + 50 = 150
ans * 2 = 300                     (using last result)
ans + 100 = 400                   (chain calculations)
```

</details>

---

<details>
<summary>Commands</summary>

### Basic Commands
- `/calc <expression>` - Calculate in chat
- `/calchist` - View calculation history (shows last 10)
- `/calcclear` - Clear calculation history

### Variable Commands
- `/calcset <var> <value>` - Set custom variable
  - Example: `/calcset profit 100m-50m`
  - Example: `/calcset stacks 10`

### Help Commands
- `/calchelp` - Show main help menu
- `/calchelp operators` - Learn about +, -, *, /, ^, %
- `/calchelp functions` - Learn about sqrt, abs, floor, ceil, round
- `/calchelp units` - Learn about k, m, b, t, s, e, h, sc, dc, eb
- `/calchelp variables` - Learn about ans and custom variables
- `/calchelp examples` - See practical examples
- `/calchelp config` - Learn about configuration

### Configuration Commands
- `/calcconfig` - View current configuration

</details>

---

<details>
<summary>Configuration</summary>

**Config file:** `config/notenoughcalculator.json`

### Available Settings
```json
{
  "maxHistorySize": 100,
  "decimalPrecision": 10,
  "showUnitSuggestions": true,
  "enableHistoryNavigation": true,
  "showInlineResults": true,
  "enableCommaFormatting": true
}
```

### Settings Explained

- **maxHistorySize** (Default: 100) - How many calculations to remember
- **decimalPrecision** (Default: 10) - Number of decimal places
- **showUnitSuggestions** (Default: true) - Show unit hints like "(50m)" in commands
- **enableHistoryNavigation** (Default: true) - Enable Ctrl+Z/Y shortcuts
- **showInlineResults** (Default: true) - Show results in REI search
- **enableCommaFormatting** (Default: true) - Format large numbers with commas

</details>

---

<details>
<summary>Keybinds</summary>

### History Navigation
- **`Ctrl + Z`** in REI search - Recall previous calculation (undo)
- **`Ctrl + Y`** in REI search - Go forward in history (redo)

### Notes
- History is session-based and automatically clears when you leave a world or server
- This ensures a fresh start each time you play!
- Use `/calchist` to view your calculation history in chat

</details>

---

<details>
<summary>Pro Tips</summary>

### Use `ans` for quick follow-ups
```
100 + 50 = 150
ans * 2 = 300
ans + 1000 = 1,300
```

### Save common values as variables
```
/calcset hourly 500k
/calcset daily $hourly * 24
$daily * 30 = 360,000,000 (monthly)
```

### Results auto-format with commas
```
1000000 → 1,000,000
```

### Mix different units
```
100m + 500k = 100,500,000
2dc + 5h = 18,432
```

### Chain complex calculations
```
(100m - 50m) * 1.1 / 64 = 859,375 (per stack after markup)
```

### Use parentheses for clarity
```
(5 + 3) * 2 = 16  (not 11)
2^(10-3) = 128    (not 1017)
```

### Create calculation workflows
```
/calcset items 1000
/calcset price 50k
$items * $price = 50,000,000 (total cost)
```

</details>

---

## Support

**Need help or found a bug?**  

Please report issues on GitHub: [Issues](https://github.com/Rijzzz/NotEnoughCalculator/issues)

You can also reach me on Discord: [rijz.gg](https://discord.com/users/1303959182201982989)

### Common Issues

- **Calculator not showing results?** Make sure REI overlay is visible (press your inventory key)
- **Red text in search bar?** This is a cosmetic issue that doesn't affect functionality
- **History not working?** Make sure `enableHistoryNavigation` is `true` in config
- **Calculator is unresponsive?** Make sure you have clicked on the REI search bar so the calculator is in focus. If you click somewhere else on the screen while typing the query, it will become unresponsive.

---

## Why This Mod?

[NEU (NotEnoughUpdates)](https://modrinth.com/mod/notenoughupdates) is one of the most popular Hypixel Skyblock mods, and its calculator feature was incredibly useful. However, NEU isn't available for newer Minecraft versions, leaving players without this essential tool.

**Not Enough Calculator** solves this problem by bringing NEU-style calculator functionality to [Roughly Enough Items (REI)](https://modrinth.com/mod/rei), which works on newer Minecraft versions.

---

## Credits

Special thanks to **[NotEnoughUpdated (NEU)](https://modrinth.com/mod/notenoughupdates)** for the original calculator inspiration! This mod brings the beloved NEU calculator experience to newer Minecraft versions.

---

## License

**All Rights Reserved** - You can use this mod in modpacks with credit. Please link to the official mod page when featuring this mod.

---

Developed by **Rijz & Laze**

**Type. Calculate. Profit.**

---

*Last updated: January 2026*
