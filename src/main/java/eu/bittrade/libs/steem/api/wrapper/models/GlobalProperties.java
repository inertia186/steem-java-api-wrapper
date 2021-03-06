package eu.bittrade.libs.steem.api.wrapper.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author http://steemit.com/@dez1337
 */
public class GlobalProperties {
    private int id;
    @JsonProperty("head_block_number")
    private long headBlockNumber;
    @JsonProperty("head_block_id")
    private String headBlockId;
    @JsonProperty("time")
    private String time;
    @JsonProperty("current_witness")
    private String currentWitness;
    @JsonProperty("total_pow")
    private long totalPow;
    @JsonProperty("num_pow_witnesses")
    private int numPowWitnesses;
    @JsonProperty("virtual_supply")
    private String virtualSupply;
    @JsonProperty("current_supply")
    private String currentSupply;
    @JsonProperty("confidential_supply")
    private String confidentialSupply;
    @JsonProperty("current_sbd_supply")
    private String currentSdbSupply;
    @JsonProperty("confidential_sbd_supply")
    private String confientialSdbSupply;
    @JsonProperty("total_vesting_fund_steem")
    private String totalVestingFundSteem;
    @JsonProperty("total_vesting_shares")
    private String totalVestingShares;
    @JsonProperty("total_reward_fund_steem")
    private String totalRewardFundSteem;
    @JsonProperty("total_reward_shares2")
    private String totalRewardShares2;
    @JsonProperty("sbd_interest_rate")
    private int sdbInterestRate;
    @JsonProperty("sbd_print_rate")
    private int sdbPrintRate;
    @JsonProperty("average_block_size")
    private int avarageBlockSize;
    @JsonProperty("maximum_block_size")
    private int maximumBlockSize;
    @JsonProperty("current_aslot")
    private long currentAslot;
    @JsonProperty("recent_slots_filled")
    private String recentSlotsFilled;
    @JsonProperty("participation_count")
    private int participationCount;
    @JsonProperty("last_irreversible_block_num")
    private long lastIrreversibleBlockNum;
    @JsonProperty("max_virtual_bandwidth")
    private String maxVirtualBandwidth;
    @JsonProperty("current_reserve_ratio")
    private int currentReserveRatio;
    @JsonProperty("vote_regeneration_per_day")
    private int voteRegenerationPerDay;

    public int getId() {
        return id;
    }

    public long getHeadBlockNumber() {
        return headBlockNumber;
    }

    public String getHeadBlockId() {
        return headBlockId;
    }

    public String getTime() {
        return time;
    }

    public String getCurrentWitness() {
        return currentWitness;
    }

    public long getTotalPow() {
        return totalPow;
    }

    public int getNumPowWitnesses() {
        return numPowWitnesses;
    }

    public String getVirtualSupply() {
        return virtualSupply;
    }

    public String getCurrentSupply() {
        return currentSupply;
    }

    public String getConfidentialSupply() {
        return confidentialSupply;
    }

    public String getCurrentSdbSupply() {
        return currentSdbSupply;
    }

    public String getConfientialSdbSupply() {
        return confientialSdbSupply;
    }

    public String getTotalVestingFundSteem() {
        return totalVestingFundSteem;
    }

    public String getTotalVestingShares() {
        return totalVestingShares;
    }

    public String getTotalRewardFundSteem() {
        return totalRewardFundSteem;
    }

    public String getTotalRewardShares2() {
        return totalRewardShares2;
    }

    public int getSdbInterestRate() {
        return sdbInterestRate;
    }

    public int getSdbPrintRate() {
        return sdbPrintRate;
    }

    public int getAvarageBlockSize() {
        return avarageBlockSize;
    }

    public int getMaximumBlockSize() {
        return maximumBlockSize;
    }

    public long getCurrentAslot() {
        return currentAslot;
    }

    public String getRecentSlotsFilled() {
        return recentSlotsFilled;
    }

    public int getParticipationCount() {
        return participationCount;
    }

    public long getLastIrreversibleBlockNum() {
        return lastIrreversibleBlockNum;
    }

    public String getMaxVirtualBandwidth() {
        return maxVirtualBandwidth;
    }

    public int getCurrentReserveRatio() {
        return currentReserveRatio;
    }

    public int getVoteRegenerationPerDay() {
        return voteRegenerationPerDay;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
