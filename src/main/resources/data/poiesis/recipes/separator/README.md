# Separator recipe JSON

Dry recipes omit `fluid`. Wet recipes include it and are only selected while
the Wet Separation Module is installed. Item inputs are flexible: declare one
or two entries. Each of up to four outputs maps to the corresponding output
slot and has an independent `chance` from `0.0` to `1.0`.

```json
{
  "type": "poiesis:separating",
  "inputs": [
    {"tag": "forge:ores/example", "count": 1}
  ],
  "fluid": {
    "tag": "minecraft:water",
    "amount": 1000
  },
  "outputs": [
    {"item": "minecraft:iron_nugget", "count": 3, "chance": 1.0},
    {"item": "minecraft:flint", "count": 1, "chance": 0.25}
  ],
  "duration": 200,
  "energy_per_tick": 20
}
```

For one exact fluid, replace `"tag"` with `"fluid"`, for example
`{"fluid": "minecraft:water", "amount": 1000}`.
