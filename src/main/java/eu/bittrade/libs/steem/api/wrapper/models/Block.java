package eu.bittrade.libs.steem.api.wrapper.models;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author http://steemit.com/@dez1337
 */
public class Block extends BlockHeader {
    @JsonProperty("witness_signature")
    private String witnessSignature;
    private List<Transaction> transactions;

    public Block(@JsonProperty("transactions") List<Transaction> transactions) {
        super();
        this.transactions = transactions;
    }

    public String getWitnessSignature() {
        return witnessSignature;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
