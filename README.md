# High Alchemy Tracker

A RuneLite Plugin Hub plugin that tracks successful High Level Alchemy casts and the gross amount of GP created.

## Features

- Lifetime cast count and gross GP created, stored separately for each RuneScape profile
- Session cast count and gross GP created
- Per-item lifetime cast and GP totals
- No item-cost, Grand Exchange-price, or nature-rune deductions
- No third-party connections or additional dependencies

Only completed casts are recorded. Selecting the spell or clicking an item without successfully casting does not change the totals.

## Storage

The plugin uses RuneLite's profile configuration storage. Each item is represented by a compact, versioned record containing its item ID, cast count, and historical gross GP. Totals are calculated from those records.

## Development

This project targets Java 11 and follows RuneLite's standard Plugin Hub build. Run the tests with:

```text
./gradlew test
```

Launch the RuneLite development client with:

```text
./gradlew run
```

The development client must be tested manually because automated interaction with RuneScape is prohibited.

## License

BSD 2-Clause. See `LICENSE`.
