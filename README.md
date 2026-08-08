# <img src="https://github.com/user-attachments/assets/51f97b1c-d2fa-4ea4-9380-3bc2e8db18d6" alt="Sousei Icon" width="30" style="filter: contrast(50);"> Sousei
<div align="center">
  <br>
  <br>
  <br>
  <img src="https://github.com/user-attachments/assets/51f97b1c-d2fa-4ea4-9380-3bc2e8db18d6" alt="Sousei Icon" width="65" style="vertical-align: middle;">
  <img alt="Sousei API" width="300" src="https://img.shields.io/badge/API-API?style=for-the-badge&label=Sousei&labelColor=%230C111735&color=%23e36c64">
</div>

# <br>
<div align="center">

A framework library mod that simplifies biome placement.\
Inject custom biomes and surface rules using simple JSON data files (optionally JAVA registrations).

[![License](https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative&logoColor=white&style=for-the-badge&labelColor=transparent&color=yellow)](LICENSE)
![NeoForge](https://img.shields.io/badge/-neoforge-orange?logo=neoforge&logoColor=white&style=for-the-badge&labelColor=transparent&color=%23e36c64)
![Version](https://img.shields.io/badge/version-1.0.0-blue?logoColor=white&style=for-the-badge&labelColor=teal&color=teal)
[![JitPack](https://img.shields.io/badge/integrate-jitpack-green?logoColor=white&style=for-the-badge&labelColor=transparent&color=seagreen)](https://jitpack.io/#AatreyuShau/Sousei)

</div>

---

<div align="center">
  
## Features
</div>

  - Universal dimension support (Overworld, Nether, The End, and custom dimensions)
  - Fast Voronoi sampling engine with smooth Simplex noise domain warping
  - Multi-condition biome filtering (Priority, Y-levels, Climate ranges, and Parent biomes)
  - Designed to minimize micro-biomes and provide better shaping control
  - Data-driven surface rules for custom top, under, and underwater blocks
  - Fully configurable via JSON datapacks
  - No client installation required; fully server-side worldgen

<div align="center">  
  
  ## How to Use
</div>

1. Create or open a datapack inside your world or mod assets directory
2. Define custom biomes in `data/<namespace>/voronoi_biomes/<entry_name>.json`
3. Define custom surface block rules in `data/<namespace>/surface_rules/<rule_name>.json`
4. Run `/reload` in-game to update biome distributions instantly without restarting

<div align="center">
  
  ## Development
</div>

```bash
# Clone the repository
git clone [https://github.com/pie-rusty/sousei.git](https://github.com/pie-rusty/sousei.git)

# Navigate to the project directory
cd sousei

# Build the mod JAR using Gradle
./gradlew build
