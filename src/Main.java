import java.io.*;
import java.util.Locale;

/**
 * Entry point and I/O driver of the GigMatch Pro application.
 *
 * Reads commands from the input file, delegates execution to PlatformEngine,
 * and writes the produced output to the output file.
 *
 * This class intentionally contains no business logic and acts only as a
 * command parser and dispatcher.
 */

public class Main {
    private static PlatformEngine platformEngine = new PlatformEngine();

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {

        String[] parts = command.split("\\s+");
        String operation = parts[0];

        try {
            String result = "";

            switch (operation) {
                case "register_customer": {
                    // Format: register_customer customerID
                    String customerid = parts[1];
                    result = platformEngine.customerAdd(customerid);
                    break;}

                case "register_freelancer":{
                    // Format: register_freelancer freelancerID serviceName basePrice T C R E A
                    // register_freelancer freelancerID serviceName basePrice T C R E A
                    String freelancerId = parts[1];
                    String serviceName = parts[2];
                    int basePrice = Integer.parseInt(parts[3]);
                    int T = Integer.parseInt(parts[4]);
                    int C = Integer.parseInt(parts[5]);
                    int R = Integer.parseInt(parts[6]);
                    int E = Integer.parseInt(parts[7]);
                    int A = Integer.parseInt(parts[8]);
                    result = platformEngine.freelancerAdd(freelancerId, serviceName, basePrice, T, C, R, E, A);
                    break;}

                case "request_job":{
                    // Format: request_job customerID serviceName topK
                    String c_id = parts[1];
                    String serviceName = parts[2];
                    int topK = Integer.parseInt(parts[3]);
                    result = platformEngine.requestJob(c_id,serviceName,topK);
                    break;}

                case "employ_freelancer":{
                    // Format: employ_freelancer customerID freelancerID
                    String customerid = parts[1];
                    String freeid = parts[2];
                    result= platformEngine.employFreelancer(customerid,freeid);
                    break;}

                case "complete_and_rate":{
                    // Format: complete_and_rate freelancerID rating
                    String f_id = parts[1];
                    int rating = Integer.parseInt(parts[2]);
                    result = platformEngine.compeleteAndRate(f_id, rating);
                    break;}

                case "cancel_by_freelancer":{
                    // Format: cancel_by_freelancer freelancerID
                    String freeid = parts[1];
                    result = platformEngine.freelancerCancel(freeid);
                    break;}

                case "cancel_by_customer":{
                    // Format: cancel_by_customer customerID freelancerID
                    String c_id = parts[1];
                    String f_id = parts[2];
                    result = platformEngine.customerCancel(c_id,f_id);
                    break;}

                case "blacklist":{
                    // Format: blacklist customerID freelancerID
                    String customerid = parts[1];
                    String freelancerId = parts[2];
                     result = platformEngine.userBlacklistFreelancer(customerid, freelancerId);
                    break;}

                case "unblacklist":{
                    // Format: unblacklist customerID freelancerID
                    String customerid = parts[1];
                    String freelancerId = parts[2];
                    result = platformEngine.userUnblacklistFreelancer(customerid,freelancerId);
                    break;}



                case "change_service":{
                    // Format: change_service freelancerID newService newPrice
                    String f_id = parts[1];
                    String newService = parts[2];
                    int newPrice= Integer.parseInt(parts[3]);
                    result = platformEngine.changeService(f_id,newService,newPrice);
                    break;}


                case "simulate_month":{
                    // Format: simulate_month
                    result = platformEngine.simulateMonth();
                    break;}

                case "query_freelancer":{
                    // Format: query_freelancer freelancerID
                    String freeId = parts[1];
                    result = platformEngine.queryFreelancer(freeId);
                    break;}

                case "query_customer":{
                    // Format: query_customer customerID
                    String customerId = parts[1];
                    result = platformEngine.queryCustomer(customerId);
                    break;}

                case "update_skill":{
                    // Format: update_skill freelancerID T C R E A
                    String freelancerId = parts[1];
                    int T = Integer.parseInt(parts[2]);
                    int C = Integer.parseInt(parts[3]);
                    int R = Integer.parseInt(parts[4]);
                    int E = Integer.parseInt(parts[5]);
                    int A = Integer.parseInt(parts[6]);
                    result = platformEngine.updateSkill(freelancerId,T,C,R,E,A);
                    break;}

                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }
}