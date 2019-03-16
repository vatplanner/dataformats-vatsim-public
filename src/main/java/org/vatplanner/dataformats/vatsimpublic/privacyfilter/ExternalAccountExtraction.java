package org.vatplanner.dataformats.vatsimpublic.privacyfilter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts what, by syntax, looks like it could possibly be an account
 * identifier for a streaming or social media service. The purpose of this
 * implementation is to identify "words" to be removed from recordings in order
 * to anonymize data as good as it can possibly be done in an automated way
 * while retaining other information. As a result, there is an accepted chance
 * of false positive matches. However, even though false positive matches occur,
 * the extracted data can never be fully complete as typos, unusual syntax and
 * syntax indistinguishable from regular, not account-related information can
 * prevent detection.
 * <p>
 * Attempting to extract account information for purposes other than automated
 * removal of such information may violate privacy laws and local regulations;
 * be careful what you apply this extraction for.
 * </p>
 * <p>
 * DISCLAIMER: THIS IS NOT LEGAL ADVICE. READ THE FULL DISCLAIMER ON PRIVACY
 * FILTERING AS PROVIDED WITH THE SOURCE CODE.
 * </p>
 *
 * @see DataFileFilterConfiguration
 */
public class ExternalAccountExtraction {

    private static final Pattern PATTERN_1 = Pattern.compile("(?:twitch(?:\\.?tv|\\.com|)|youtu\\.?be(?:\\.com|)|discord(?:\\.gg|\\.me)/|facebook\\.com|hitbox\\.tv|smashcast\\.tv|mixer\\.com)(?:\\s*(?:stream(?:er|)|/?channel|/?user|/?groups|/c/|/watch\\?v=|)\\s*|)[_/ \\[=]*([^/^§$\\] \\?]+)", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_1_ACCOUNT = 1;

    private static final Pattern PATTERN_2 = Pattern.compile("([a-z0-9_\\-\\.\"']+) on (?:youtube|twitter|twitch|facebook|discord)", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_2_ACCOUNT = 1;

    private static final Pattern PATTERN_3 = Pattern.compile("(?:youtube|twitter|twitch(?:[\\. /]*tv|)|facebook)(?:[ \\-]*(?:channel|user|page|live|(?:live[- ]*|)stream(?:ing|)|/|)[ \\-]*)[\"'@\\*\\-_= \\|><]+([a-z0-9_\\-\\. ]+)", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_3_ACCOUNT = 1;

    private static final Pattern PATTERN_4 = Pattern.compile("(?:youtube|twitter|twitch(?:\\s?tv|)|facebook|instagram)[^a-z]*?(?:[@/]|at)([a-z0-9_\\-\\. ]+)", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_4_ACCOUNT = 1;

    private static final Pattern PATTERN_NOACCOUNT = Pattern.compile("https?|[vtr]|[^a-z0-9]+|stream(|ing|s)|live|live[ \\-]*stream(|ing)|channel|page|vacc|this|call\\s*sign|twitch(\\.tv|\\.com|)|youtube(\\.com|)|.{1,2}|.{30,}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_EXTRA_CHARS = Pattern.compile("[;@\\s\\.+*\\-_/\"'(=\\[]*(?:watch\\?v=|)(.*?)[\\s\\.+*\\-_/\"')=\\]]*", Pattern.CASE_INSENSITIVE);
    private static final int PATTERN_EXTRA_CHARS_KEEP = 1;

    private boolean checkPossibleAccount(String s) {
        Matcher matcher = PATTERN_NOACCOUNT.matcher(s);
        return !matcher.matches();
    }

    /**
     * Extracts everything that looks like an account identifier from the given
     * field. See class JavaDoc for more information and disclaimer.
     *
     * @param fieldContent field content to extract information from
     * @return all account identifiers, empty if nothing was found
     */
    public Set<String> extractAccounts(String fieldContent) {
        HashSet<String> accounts = new HashSet<String>();

        extractByPattern(fieldContent, accounts, PATTERN_1, PATTERN_1_ACCOUNT, true);
        extractByPattern(fieldContent, accounts, PATTERN_2, PATTERN_2_ACCOUNT, false);
        extractByPattern(fieldContent, accounts, PATTERN_3, PATTERN_3_ACCOUNT, false);
        extractByPattern(fieldContent, accounts, PATTERN_4, PATTERN_4_ACCOUNT, false);

        return accounts;
    }

    private String removeExtraChars(String s) {
        Matcher matcher = PATTERN_EXTRA_CHARS.matcher(s);
        if (!matcher.matches()) {
            return s;
        }

        return matcher.group(PATTERN_EXTRA_CHARS_KEEP);
    }

    private void extractByPattern(String fieldContent, HashSet<String> accounts, Pattern pattern, int matcherGroup, boolean incrementalSearch) {
        Matcher matcher = pattern.matcher(fieldContent);

        if (!incrementalSearch) {
            while (matcher.find()) {
                extractFromMatch(matcher, matcherGroup, accounts);
            }
        } else {
            int startIndex = 0;
            while (startIndex < fieldContent.length() && matcher.find(startIndex)) {
                startIndex = matcher.start() + 1;

                extractFromMatch(matcher, matcherGroup, accounts);
            }
        }
    }

    private void extractFromMatch(Matcher matcher, int matcherGroup, HashSet<String> accounts) {
        String account = matcher.group(matcherGroup);

        account = removeExtraChars(account);

        if (account.isEmpty() || !checkPossibleAccount(account)) {
            return;
        }

        accounts.add(account);
    }
}
