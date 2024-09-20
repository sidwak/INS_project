import java.io.*;
import java.util.*;

class DeBlock{
	private Integer blockData;
    private Long preBlockData;   
    private Long postBlockData;

    public Long getPreBlockData() {
        return preBlockData;
    }   
    public void setPreBlockData(Long preBlockData) {
        this.preBlockData = preBlockData;
    }
    public Long getPostBlockData() {
        return postBlockData;
    }
    public void setPostBlockData(Long postBlockData) {
        this.postBlockData = postBlockData;
    }

    public DeBlock() {
    	super();
    }

    private Integer bytesToInt(Byte b1, Byte b2, Byte b3, Byte b4) {
        return ((b1 & 0xFF) << 24) | 
               ((b2 & 0xFF) << 16) | 
               ((b3 & 0xFF) << 8)  | 
               (b4 & 0xFF);
    }

    public Integer getBlockData() {
        return blockData;
    }
    public void setBlockData(Integer blockData) {
        this.blockData = blockData;
    }
}

class DeRound{
	final boolean isDebug = false;
    final List<Integer> arrayOf32s = new ArrayList<Integer>(Arrays.asList(29, 18, 3, 0, 21, 20, 5, 19
    , 13, 2, 25, 4, 8, 23, 14, 1, 6, 15, 12, 17, 28, 26, 24, 11, 27, 30, 9, 7, 16, 10, 22, 31));
    
    DeBlock rBlock;
    
    Integer key;
    
    public DeRound(DeBlock nBlock, Integer nKey){
    	super();
        this.rBlock = nBlock;
        this.key = nKey;       
        this.startRound();
    }

    private void startRound(){
        this.performRandomness();
        this.performRailfenceTransposition();
        this.performKeyXOR();
        this.performBitsSubstitution();
        this.performInitialPermutation();
    }

    private void performInitialPermutation(){
    	Integer preShift = this.rBlock.getBlockData();
    	if (isDebug == true)
        	System.out.println("Reverting Initial Permutation");
        if (isDebug == true)
        	System.out.println("Pre__shift: " + Integer.toString(preShift, 2) + " Length: " + Integer.toString(preShift, 2).length());
        Integer oldNumber = 0;
        for (int i = 0; i <= 31; i++){
            int oldPosition = arrayOf32s.get(i);
            Integer bit = (preShift >> oldPosition) & 1;
            //System.out.println(i + " " + Integer.toString(bit, 2) + " " + Integer.toString(oldNumber, 2));
            oldNumber |= (bit << i);
        }
        if (isDebug == true)
        	System.out.println("Post_shift: " + Integer.toString(oldNumber, 2) + " Length: " + Integer.toString(oldNumber, 2).length() + "\n");
        rBlock.setBlockData(oldNumber);
    }

    private void performBitsSubstitution(){
        Dictionary<String, String> subTable = new Hashtable<>();
        //can be derived from key
        subTable.put("01", "00");
        subTable.put("11", "01");
        subTable.put("00", "10");
        subTable.put("10", "11");

        StringBuilder sb = new StringBuilder();
        StringBuilder subString = new StringBuilder();
        Integer beforeSub = rBlock.getBlockData();
        for (int i = 0; i < 31; i += 2){
            Integer bit_r = (beforeSub >> i) & 1;
            Integer bit_l = (beforeSub >> (i + 1)) & 1;
            String concat = Integer.toString(bit_l) + Integer.toString(bit_r);
            //System.out.print(concat + " ");       
            sb.insert(0, concat);
            subString.insert(0, subTable.get(concat));
        }
        //System.out.println("This is reversed");
        if (isDebug == true) {
        	System.out.println("Reverting Substitution");
        	System.out.println("Before Subs: " + sb + " Length: " + sb.length());
        	System.out.println("After_ Subs: : " + subString + " Length: " + subString.length() + "\n");
        }
        rBlock.setBlockData(Integer.parseUnsignedInt(subString.toString(), 2));
    }

    private void performKeyXOR(){
    	if (isDebug == true)
        	System.out.println("Performing Key XOR");
    	if (isDebug == true)
    		System.out.println("Before XOR: " + Integer.toString(rBlock.getBlockData(), 2) + " Length: " + Integer.toString(rBlock.getBlockData(), 2).length());
        Integer xorVal = rBlock.getBlockData() ^ this.key;
        if (isDebug == true)
        	System.out.println("After_ XOR: " + Integer.toString(xorVal, 2) + " Length: " + Integer.toString(xorVal, 2).length() + "\n");
        rBlock.setBlockData(xorVal);
    }

    private void performRailfenceTransposition(){
    	Integer prefill = rBlock.getBlockData();
    	Random rdn = new Random(this.key);
    	Integer fenceKey = rdn.nextInt(8);
    	//System.out.println("Fence Key: " + fenceKey.toString());
        String[][] fence = new String[32][fenceKey];

        for (int i = 0; i < 32; i++){
            for (int j = 0; j < fenceKey; j++){
                fence[i][j] = ".";
            }
        }             

        int c = 0;
        for (int i = 31; i >= 0; i--){
            fence[31 -  i][c] = "0";
            c++;
            if (c == fenceKey){ c = 0; };
        }
        /*for (int i = 0; i < fenceKey; i++){
            for (int j = 0; j < 32; j++){
                System.out.print(fence[j][i] + " ");
            }
            System.out.println("\n");
        }*/
        Integer shifted = 0;
        for (int i = 0; i < fenceKey; i++) {
        	for (int j = 0; j < 32; j++) {
        		if (fence[j][i].equals("0") == true) {
            		int bit = (prefill >> (31 - shifted)) & 1;
            		fence[j][i] = Integer.toString(bit);
            		shifted += 1;
            	}
        	}
        }
        int k = 0;
        StringBuilder railDecr = new StringBuilder();
        for (int i = 31; i >= 0; i--){
            railDecr.append(fence[31 -  i][k]);
            k++;
            if (k == fenceKey){ k = 0; };
        }
        
        /*for (int i = 0; i < fenceKey; i++){
            for (int j = 0; j < 32; j++){
                System.out.print(fence[j][i] + " ");
            }
            System.out.println("\n");
        }*/
        if (isDebug == true) {
        	System.out.println("Reverting RailFence Transposition");
        	System.out.println("After RailFence: "+railDecr.toString() + "\n");
        }
        rBlock.setBlockData(Integer.parseUnsignedInt(railDecr.toString(), 2));
    }

    private String[] generateBitArray(long seed) {
        Random random = new Random(seed);
        String[] bitArray = new String[8];       
        for (int i = 0; i < 8; i++) {
            int bit = random.nextInt(2);
            bitArray[i] = Integer.toString(bit);
        }      
        return bitArray;
    }

    private List<Integer> generateBitLocations(long seed){
        List<Integer> pos = new ArrayList<Integer>(Arrays.asList(0 ,1, 2, 3, 4, 5, 6, 7));
        //shuffle here
        Collections.shuffle(pos, new Random(seed));
        return pos;
    }

    private void performRandomness(){
        StringBuilder randomizeStr = new StringBuilder();
        Long beforeAdd = rBlock.getPreBlockData();
        for (int i = 39; i >= 0; i--){
            Long bit = (beforeAdd >> i) & 1;
            randomizeStr.append(bit);
        }
        if (isDebug == true) {
        	System.out.println("Performing Randomness:");
        	System.out.println("Before randomness: " + randomizeStr + " Length: " + randomizeStr.length());
        }
        List<Integer> randomizePos = this.generateBitLocations(this.key);
        Collections.reverse(randomizePos);
        for (int i = 0; i < 8; i++){
            //randomizeStr.insert(randomizePos.get(i), "1");
            randomizeStr.deleteCharAt(randomizePos.get(i) * 4);
        }
        if (isDebug == true)
        	System.out.println("After_ randomness: " + randomizeStr.toString() + " Length: " + randomizeStr.toString().length());
        rBlock.setBlockData(Integer.parseUnsignedInt(randomizeStr.toString(), 2));
        //this.finishRound(randomizeStr.toString());
    }

    private void finishRound(String longStr){
        Long finalData = Long.parseLong(longStr, 2);
        rBlock.setPostBlockData(finalData);
    }
}

class Decryption{
	private Integer key = 0;
    private String plaintext = "";
    private List<DeBlock> pBlocks = new ArrayList<DeBlock>();

    
    public Decryption(String nKey){
        this.key = this.parseKey(nKey);
        this.startAlgorithm();
    }

    private void startAlgorithm(){
    	this.readCipherDataFromFile();
        this.performRounds();
        this.readDataFromBlocks();
    }
    
    private void performRounds(){
    	int ctr = 0;
        for (DeBlock iBlock: this.pBlocks){
        	System.out.println("Initiating Block Processing ID: " + Integer.toString(ctr) + "...\n");
            DeRound iRound = new DeRound(iBlock, this.key);
            System.out.println("Block Processed ID: " + Integer.toString(ctr) + "\n\n");
            ctr += 1;
        }
    }
    
    private void readDataFromBlocks() {
    	StringBuilder plaintext = new StringBuilder();
    	for (DeBlock iBlock: this.pBlocks){
            Integer blockData = iBlock.getBlockData();
            Integer byte1 = blockData >> 24;
        	plaintext.append((char)(int)byte1);
        	Integer byte2 = (blockData >> 16) & 0xFF;
        	plaintext.append((char)(int)byte2);
        	Integer byte3 = (blockData >> 8) & 0xFF;
        	plaintext.append((char)(int)byte3);
        	Integer byte4 = blockData & 0xFF;
        	plaintext.append((char)(int)byte4);
        }
    	System.out.println("100% decrypting data...\n");
    	System.out.println("Plaintext:\n\n" + plaintext.toString());
    }

    private void readCipherDataFromFile(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("encryptedData.txt"));
            String line = br.readLine();
            while (line != null) {
            	//System.out.println("Read line: " + line);
            	DeBlock block = new DeBlock();
            	block.setPreBlockData(Long.parseLong(line, 2));
            	this.pBlocks.add(block);
            	line = br.readLine();
            }
            System.out.println("Finished reading data from file");
        }
        catch (Exception e){

        }
    }

    private Integer parseKey(String nKey){
        Integer parsedKey = Integer.parseInt(nKey);
        return parsedKey;
    }
}

public class decrypter {

	public static void main(String[] args){
       takeInput();
    }
	
    public static void takeInput(){
        //String key = "432435432";
        String plaintext = "abceefg";
        Scanner myScan = new Scanner(System.in);
        System.out.println("Enter a Integer (max 32 bits): ");
        String key = myScan.nextLine();
        System.out.println("Entered key is: " + key);
        Decryption decrypt = new Decryption(key);
        //System.out.println(Integer.parseInt("01100001011000100110001101100101",2));
    }

}
