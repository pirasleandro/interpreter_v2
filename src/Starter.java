public class Starter {
    public static void main(String[] args) throws Exception {
        LolcodeInterpreter interpreter = new LolcodeInterpreter(new String[]{
            "HAI 1.0",
            "I HAS A test ITZ 10",
            "VISIBLE test",
            "KTHXBYE"
        });
        interpreter.interpret();
    }
}
