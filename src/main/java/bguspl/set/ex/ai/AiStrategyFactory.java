package bguspl.set.ex.ai;

public class AiStrategyFactory {

    public static AiStrategy forLevel(String level) {
        if (level == null) return new EasyAiStrategy();
        switch (level.toLowerCase()) {
            case "hard":
                return new HardAiStrategy();
            case "medium":
                return new MediumAiStrategy();
            default:
                return new EasyAiStrategy();
        }
    }
}
