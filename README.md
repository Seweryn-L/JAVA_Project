# Brick Transport Simulation

**A JavaFX application simulating a brick factory workflow**, where workers load bricks onto a conveyor belt, which transports them to a truck.
The simulation enforces limits on the number and total weight of bricks on the belt, and the truck can only be loaded if it has enough capacity.


## Features

* **Conveyor Belt**: Transports bricks with configurable maximum count and weight limits.
* **Workers**: Multiple workers add bricks of different weights at random intervals.
* **Truck**: Loads bricks from the belt and unloads automatically when full.
* **GUI**: Visualizes the workflow with animations for belt movement, brick transport, and truck unloading.
* **Notifications**: Toast notifications show worker activity and system status.
* **Configuration**: Loads settings from a YAML file or uses default values.
  
## Screenshots

![3 workers](https://github.com/user-attachments/assets/3437e658-8583-4b75-aa70-677275e026d2)
![Simulation View](https://github.com/user-attachments/assets/a269267b-f47d-4b5f-a631-98c3ff168077)


## How It Works

1. **Workers** place bricks on the conveyor belt at random intervals.
2. The **conveyor belt** moves bricks toward the truck.
3. The **truck** loads bricks if it has capacity; otherwise, it **unloads automatically**.
4. The **GUI** displays:

   * Current brick count and weight on the belt.
   * Current truck load.
   * Animations and system notifications.


## Configuration

The application is configured via `config.yaml` (see `config/` directory for default values):

### Example settings:

* **Belt**:

  * `maxBrickCount`: Maximum number of bricks.
  * `maxTotalWeight`: Maximum total weight on the belt (kg).

* **Truck**:

  * `maxCapacity`: Maximum weight it can carry (kg).

* **Workers**:

  * Each worker has:

    * `brickMass`: Weight of the brick (kg).
    * `intervalRange`: Random interval (min/max) between placing bricks.

> If no configuration file is found, default values are used.


## Getting Started

### Clone the repository:

```bash
git clone https://github.com/yourusername/brick-transport-simulation.git
cd brick-transport-simulation
```
