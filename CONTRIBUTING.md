# Contributing

Thanks for wanting to help out with Not Enough Calculator 🧮

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
3. Open it in your preferred IDE
4. Run `./gradlew build` to make sure it works

### Making a PR

1. Create a branch off the latest: `git checkout -b fix/your-fix-name`
2. Make your changes and test them in-game
3. Commit with a clear message describing what you changed
4. Push and open a Pull Request

### Code style

* 4 spaces, no tabs
* Clear naming (`calculateResult` not `cr`)
* Comment anything that isn't obvious
* Run `./gradlew spotlessApply` before committing to format your code automatically
* All `.java` files need the LGPL header at the top, don't remove or modify it:

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

## Translations

Want to add a language?

1. Copy `src/main/resources/assets/notenoughcalculator/lang/en_us.json`
2. Rename to your language code (e.g. `de_de.json`, `fr_fr.json`)
3. Translate the values, not the keys
4. Open a PR

## Questions?

[Join the Discord](https://discord.gg/asPJ4qgs8q) if you need help or want to discuss something before opening a PR.

## License

By contributing, you agree your code will be licensed under [LGPL-3.0-or-later](https://github.com/Rijzzz/NotEnoughCalculator/blob/26.2/LICENSE.txt).