import java.util.ArrayList;
import java.util.List;

public class EncounterFactory {

    public static List<Enemy> createInitialEnemies(int difficulty) {
        List<Enemy> enemies = new ArrayList<>();
        switch (difficulty) {
            case 2:
                enemies.add(new Goblin());
                enemies.add(new Wolf());
                break;
            case 3:
                enemies.add(new Goblin());
                enemies.add(new Goblin());
                break;
            case 1:
            default:
                enemies.add(new Goblin());
                enemies.add(new Goblin());
                enemies.add(new Goblin());
                break;
        }
        return enemies;
    }

    public static List<Enemy> createBackupEnemies(int difficulty) {
        List<Enemy> backups = new ArrayList<>();
        switch (difficulty) {
            case 2:
                backups.add(new Wolf());
                backups.add(new Wolf());
                break;
            case 3:
                backups.add(new Goblin());
                backups.add(new Wolf());
                backups.add(new Wolf());
                break;
            case 1:
            default:
                break;
        }
        return backups;
    }
}
