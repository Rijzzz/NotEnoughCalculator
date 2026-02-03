# Contributing

Hey! Thanks for checking out Not Enough Calculator 🧮

## Found a Bug?

[Open an issue](https://github.com/Rijzzz/NotEnoughCalculator/issues/new) and tell us:
* What happened
* What you expected
* How to reproduce it
* Your Minecraft version and mod version

## Have an idea?

[Open an issue](https://github.com/Rijzzz/NotEnoughCalculator/issues/new) and share your idea!

## Want to contribute code?

### Setup
1. Fork the repo
2. Clone it: `git clone https://github.com/Rijzzz/NotEnoughCalculator.git`
3. Open in IntelliJ IDEA
4. Run `./gradlew build` to make sure it works

### Make changes
1. Create a branch: `git checkout -b fix/your-fix-name`
2. Make your changes
3. Test in-game: `./gradlew runClient`
4. Commit: `git commit -m "Fix: describe what you fixed"`
5. Push: `git push origin fix/your-fix-name`
6. Open a Pull Request

### Code style
* Use 4 spaces for indentation
* Name things clearly (`calculateResult` not `cr`)
* Add comments for complex stuff
* Every `.java` file needs the LGPL license header at the top - **keep it intact, don't modify it:**
```java
/*
 * This file is part of Not Enough Calculator.
 *
 * Not Enough Calculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Not Enough Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
```

### Key files
* `CalculatorManager.java` - Main logic
* `ExpressionEvaluator.java` - Math engine
* `ResultFormatter.java` - Number formatting
* `CalcCommands.java` - Commands like /calc

## Want to translate?

1. Copy `src/main/resources/assets/notenoughcalculator/lang/en_us.json`
2. Rename it to your language code (like `de_de.json`, `fr_fr.json`, `en_au.json`)
3. Translate the values (not the keys!)
4. Submit a PR

## Need help?

[Join our Discord](https://discord.gg/asPJ4qgs8q) and ask!

## License

By contributing, you agree your code will be licensed under [LGPL-3.0-or-later](https://github.com/Rijzzz/NotEnoughCalculator/blob/1.21.9-1.21.11/LICENSE.txt).

---

That's it! Keep it simple, test your changes, and we'll help if you get stuck!