package io.smallrye.graphql.test.apps.profile.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Create some test date for Profiles
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ProfileDB {
    private static final Map<Integer, Profile> DB = new HashMap<>();

    private static Random random = new Random();

    public static List<Profile> getProfiles() {
        return new ArrayList<>(DB.values());
    }

    public static Profile getProfile(int id) {
        return DB.get(id);
    }

    public static Profile addProfile(Profile profile) {
        if (profile.getId() == null)
            profile.setId(getRandomId());
        DB.put(profile.getId().intValue(), profile);
        return profile;
    }

    static {
        DB.put(1, createProfile(1));
        DB.put(2, createProfile(2));
        DB.put(3, createProfile(3));
        DB.put(4, createProfile(4));
        DB.put(5, createProfile(5));
    }

    private static Long getRandomId() {
        long nextLong = random.nextLong();
        if (DB.containsKey(nextLong)) {
            return getRandomId();
        }
        return nextLong;
    }

    private static Profile createProfile(int i) {
        try {
            Profile p = new Profile();
            p.setId(Integer.valueOf(i).longValue());
            p.setLocale(Locale.UK.toString());
            p.title = "Mr"; // Test public fields
            p.addName("Phillip");
            p.setSurname("Kruger");
            p.setUsername("phillip.kruger");
            p.setIdNumber(IdNumber.fromString("ABC123456789"));
            p.addProfilePicture(new URL(
                    "https://avatars1.githubusercontent.com/u/6836179?s=460&u=8d34b60fb495daf689775b2c12ded76760cdb508&v=4"));
            p.setBirthDate(new SimpleDateFormat("yyyyMMdd").parse("19780703"));
            p.setMemberSinceDate(new SimpleDateFormat("yyyyMMdd").parse("20050101"));
            p.setFavColor("Green");
            p.setEmail(new Email("phillip.kruger@redhat.com"));
            Website website = new Website();
            website.setValue("https://www.phillip-kruger.com");
            p.setWebsite(website);
            p.addTagline("Everything in it's right place");
            p.addTagline("I might be wrong");
            p.setBiography(LONG_TEXT);
            p.setOrganization("Red Hat");
            p.setOccupation("Software Engineer");
            p.addInterest("Squash");
            p.addInterest("Rugby");
            p.addInterest("Worm farm");
            p.addSkill("Java");
            p.addSkill("Jakarta EE");
            p.addSkill("MicroProfile");
            p.addSkill("GraphQL");
            p.setJoinDate(new java.sql.Date(1586374533106L));
            p.setAnniversary(new java.sql.Date(1586374533106L));
            p.setMaritalStatus("maried");
            p.setUserAgent("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:74.0) Gecko/20100101 Firefox/74.0");
            p.addCreditCard("5678123409873231"); // Test transient fields
            return p;
        } catch (MalformedURLException | ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final String LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam non eleifend quam, iaculis bibendum enim. Morbi eget lorem non quam lobortis molestie. Proin pretium malesuada aliquam. Nulla commodo est nec eros placerat dictum. In ut justo vulputate, rhoncus mi in, eleifend nulla. Sed et nulla feugiat, posuere elit eget, lobortis felis. Praesent nisl orci, rutrum euismod sollicitudin eu, lacinia vitae ex. Aliquam suscipit interdum magna vitae eleifend. Nulla nunc ex, accumsan sit amet cursus sit amet, pellentesque et neque. Maecenas tincidunt eu nunc nec congue. Quisque tempor bibendum lorem, id rutrum ipsum dapibus in. Nulla finibus scelerisque nisl, ac finibus dolor fermentum sed. Nunc tempus libero pharetra imperdiet porttitor.\n"
            +
            "\n" +
            "Mauris sit amet mattis dui. Sed luctus malesuada nisl, sed fringilla sapien tincidunt et. Etiam porttitor suscipit mi, nec malesuada eros iaculis vitae. Nulla ex lorem, fermentum vitae nisl ac, efficitur rhoncus ligula. Fusce egestas condimentum porttitor. Pellentesque vestibulum eu turpis eu pellentesque. Fusce in tristique urna, vitae imperdiet magna. Praesent venenatis vitae nunc ut commodo. Integer imperdiet malesuada tellus sed lobortis. Nunc malesuada venenatis elit at dapibus. Fusce et luctus augue, nec luctus lacus. Etiam fermentum sapien vitae maximus accumsan. Cras eget ante sed massa efficitur porta.\n"
            +
            "\n" +
            "Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed ipsum elit, varius vel orci sit amet, aliquet cursus libero. Vestibulum finibus placerat aliquam. Praesent in eros ligula. In pretium est id pharetra blandit. Suspendisse tristique, mauris elementum imperdiet aliquam, est justo rhoncus sapien, in maximus est lacus a nisl. Morbi nec ultrices massa, sit amet sagittis tellus. Nullam at urna rhoncus, elementum nisi ultricies, efficitur ipsum. Integer sagittis pulvinar arcu, eu fringilla felis maximus eget. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Proin tellus nisi, finibus vitae interdum fringilla, ultricies a libero. Phasellus pretium tincidunt augue non condimentum. Donec suscipit erat justo, nec interdum augue vulputate ac.\n"
            +
            "\n" +
            "Sed finibus pellentesque tellus, a ultrices libero aliquam molestie. Vivamus placerat lectus ut nisi pulvinar dictum. Nunc porttitor, neque finibus rhoncus venenatis, ipsum nibh feugiat leo, quis aliquam diam lorem placerat felis. Nullam nec mollis magna. Nam lacus erat, mollis non ornare ut, mattis eget urna. Suspendisse pulvinar molestie purus sed vestibulum. Duis hendrerit quam ut massa interdum, id posuere arcu fermentum. Sed est eros, vestibulum in condimentum ac, imperdiet et ante. Aliquam eget lacus urna. Quisque a blandit nibh, in rutrum tortor. Donec pulvinar, nunc elementum auctor tempus, nisl neque efficitur neque, ut volutpat lorem quam sollicitudin ligula.\n"
            +
            "\n" +
            "Nunc tincidunt purus quis enim condimentum ullamcorper. Pellentesque et luctus ligula. Ut iaculis, erat vitae commodo pellentesque, lectus nulla malesuada ante, sit amet tristique tortor quam et lectus. Aliquam erat orci, ultrices ut tortor vitae, scelerisque laoreet nisl. Nulla facilisi. Donec enim mi, pharetra sit amet dapibus ut, molestie ut ipsum. Integer auctor nisl ligula. Donec sit amet tempus tortor. Quisque iaculis justo eget arcu gravida, sed porttitor quam fringilla. Nulla eu nisi finibus libero dictum auctor. Ut elementum auctor justo, a volutpat mi rhoncus at. Praesent fringilla enim nec magna semper, vel varius elit vulputate. ";
}
