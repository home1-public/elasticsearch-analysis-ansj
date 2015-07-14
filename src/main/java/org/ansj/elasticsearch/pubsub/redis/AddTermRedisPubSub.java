package org.ansj.elasticsearch.pubsub.redis;


import org.ansj.library.UserDefineLibrary;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;
import redis.clients.jedis.JedisPubSub;

public class AddTermRedisPubSub extends JedisPubSub {

    public static ESLogger log = Loggers.getLogger("ansj-redis-msg");

    @Override
    public void onMessage(final String channel, final String message) {
        if (log.isDebugEnabled()) {
            log.debug("channel:" + channel + " and message:" + message);
        }
        final String[] fragments = message.split(":");
        if (fragments.length != 3) {
            return;
        }
        final String dic = fragments[0];
        final String act = fragments[1];
        final String val = fragments[2];
        final UserDefineLibrary userDefineLibrary = UserDefineLibrary.getInstance();
        switch (dic) {
            case "u":
                switch (act) {
                    case "c":
                        userDefineLibrary.insertWord(val, "userDefine", 1000);
                        FileUtils.append(val);
                        break;
                    case "d":
                        userDefineLibrary.removeWord(val);
                        FileUtils.remove(val);
                        break;
                    default:
                        break;
                }
                break;
            case "a":
                switch (act) {
                    case "c":
                        final String[] cmd = val.split("-");
                        final Value value = new Value(cmd[0], cmd[1].split(","));
                        Library.insertWord(userDefineLibrary.getAmbiguityForest(), value);
                        FileUtils.appendAMB(val.replace(",", "\t").replaceAll("-", "\t"));
                        break;
                    case "d":
                        Library.removeWord(userDefineLibrary.getAmbiguityForest(), val);
                        FileUtils.removeAMB(val);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPMessage(final String pattern, final String channel, final String message) {
        if (log.isDebugEnabled()) {
            log.debug("pattern:" + pattern + " and channel:" + channel + " and message:" + message);
        }
        onMessage(channel, message);
    }

    @Override
    public void onPSubscribe(final String pattern, final int subscribedChannels) {
        if (log.isDebugEnabled()) {
            log.debug("psubscribe pattern:" + pattern + " and subscribedChannels:" + subscribedChannels);
        }

    }

    @Override
    public void onPUnsubscribe(final String pattern, final int subscribedChannels) {
        if (log.isDebugEnabled()) {
            log.debug("punsubscribe pattern:" + pattern + " and subscribedChannels:" + subscribedChannels);
        }

    }

    @Override
    public void onSubscribe(final String channel, final int subscribedChannels) {
        if (log.isDebugEnabled()) {
            log.debug("subscribe channel:" + channel + " and subscribedChannels:" + subscribedChannels);
        }

    }

    @Override
    public void onUnsubscribe(final String channel, final int subscribedChannels) {
        if (log.isDebugEnabled()) {
            log.debug("unsubscribe channel:" + channel + " and subscribedChannels:" + subscribedChannels);
        }
    }
}
