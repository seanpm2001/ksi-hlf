
package org.guardtime.ksi.hlf.ledgerapi;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Context;

public class StateList {
    private Context ctx;
    private StateConstructor construct;
    private String nameSpace;

    /**
     * ctx - HLF context.
     * namespace - suffix of the HLF ledger key.
     * construct - a functional interface for constructing empty State objects.
     */
    public StateList(Context ctx, String namespace, StateConstructor construct) {
        if (ctx == null) throw new NullPointerException("StateList Context is null!");
        if (namespace == null) throw new NullPointerException("StateList namespace is null!");
        if (namespace.length() == 0) throw new IllegalArgumentException("StateList namespace length is 0!");
        if (construct == null) throw new NullPointerException("StateList State constructor is null!");
        
        this.ctx = ctx;
        this.nameSpace = namespace;
        this.construct = construct;
    }
    
    public String getFullKey(String key) {
        if (key == null) throw new NullPointerException("To constructing full key, input key must not be null!");
        if (key.length() == 0) throw new IllegalArgumentException("To constructing full key, input key must not be empty string!");
        return nameSpace + "." + key;
    }

    public void setState(State s) throws LedgerApiException {
        if (s == null) throw new NullPointerException("Unable to set null State!");
        String ledgerKey = getFullKey(s.getKey());
        
        System.out.println("Getting state: " + this.nameSpace);
        System.out.println("Ledger Key is: " + ledgerKey);
        
        try {
            byte[] data = s.serialize();
            System.out.println(new String(data, UTF_8));
    
            this.ctx.getStub().putState(ledgerKey, data);
            return;
        } catch (Exception e) {
            throw new LedgerApiException("Unable to set data at key: " + ledgerKey + "!", e);
        }
    }

    public State getState(String key) throws LedgerApiException, LedgerApiNoDataException {
        String ledgerKey = getFullKey(key);
        
        System.out.println("Getting state: " + this.nameSpace);
        System.out.println("Ledger Key is: " + ledgerKey);
        
        try {
            State tmp = construct.make();
            byte[] data = this.ctx.getStub().getState(ledgerKey);
            System.out.println("get state dump::");
            System.out.println(new String(data, UTF_8));
     
            if (data == null || data.length == 0) {
                 throw new LedgerApiNoDataException("Key: " + ledgerKey + " does not contain any data!");
            }
            
            return tmp.parse(data);
        } catch (LedgerApiNoDataException e) {
            throw e;
        } catch (Exception e) {
            throw new LedgerApiException("Unable to get data from key: " + ledgerKey + "!", e);
        }
    }
}