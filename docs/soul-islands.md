## Customizing Soul Home Starting Islands

When a player first uses a Soul Key, a screen will offer them a choice of starting islands. These islands can be overwritten via datapack!

### Pack layout
```
my_islands/
├── pack.mcmeta
└── data/
    └── mypack/
        ├── soul_island/
        │   └── mushroom_hut.json  //the picker entry
        └── structure/
            └── mushroom_hut.nbt   //the structure itself
```
Screenshots are textures, so they go in a **resource pack**, not the datapack:
```
my_islands/
├── pack.mcmeta
└── assets/mypack/textures/gui/soul_island/mushroom_hut.png
```
If an island doesn't have a screenshot, the screen shows "No preview available" for that island.

### The island entry
```json
{
  "structure": "mypack:mushroom_hut",
  "screenshot": "mypack:textures/gui/soul_island/mushroom_hut.png",
  "name": "Mushroom Hut",
  "description": "A squishy little home under a giant mushroom.",
  "order": 5,
  "spawn": [12, 9, 10]
}
```
| Field | Required | What it does |
|---|---|---|
| `structure` | yes | The structure to build. |
| `screenshot` | yes | The picture shown in the picker. |
| `name` | yes | Plain text, or you can do translate keys if you want. |
| `description` | no | Text shown under the name. |
| `order` | no | Position in the picker, lowest first. Defaults to 0. |
| `spawn` | no | Where players arrive (see below). |
| `enabled` | no | Set to `false` to hide the island. idk why you'd want this. |

### Replacing or hiding the built-in islands
This mod comes with the following entries in `data/broom_closet/soul_island/`:

| File | Island |
|---|---|
| `island0.json` | Blank Slate |
| `island2.json` | Warped Isle |
| `island3.json` | Spruce Pond |
| `island4.json` | Placeholder (already hidden) |

- **To replace one:** put your own file at the same path in your datapack.
- **To hide one:** make the file contain just `{"enabled": false}`.

If every island is hidden, the picker doesn't appear and SoulHome picks a random island based on UUID like it normally does. Note that this is broken in the main mod, so use with caution.

### Building the structure
- Save it with a structure block. Vanilla structure blocks max out at 48×48×48.
- SoulHome places the island so its **top layer** ends up at y=69, centered on x/z 0,0.
- **Arrival:** without a `spawn`, players arrive above the middle of the island and are set down on whatever is directly below them. Make sure there's solid ground or water under the center; if there's nothing there, they fall into the void.
- **`spawn`:** with a `spawn`, players arrive at that spot instead. Use the same coordinates a structure block shows, counted from the structure's corner. `y` is where the player's feet go.

### Good to know
- **Picks are permanent.** Changing the datapack later doesn't affect homes that already exist, only players who haven't picked yet.
- **Reloading:** run `/reload` after changing entries. If a structure edit doesn't show up, restart the world.
- **Turning the picker off:** set `soulhomeIslandPicker = false` in `config/broom_closet/common.toml`. Players who already picked still get their island.
