package io.smallrye.graphql.test.apps.profile.api;

import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.DateFormat;

public class Profile {
    public static String uuid = UUID.randomUUID().toString();
    private Integer id;
    private String locale;
    public String title;
    private List<String> names;
    private List<String> nicknames;
    private String surname;
    private String username;
    private String idNumber;
    private List<URL> coverphotos;
    private List<URL> profilePictures;

    //    private Gender gender;
    @DateFormat("dd/MM/yyyy")
    private Date birthDate;
    private Date memberSince;
    private String favColor;
    //    private List<Address> addresses;
    private List<String> emailAddresses;
    //    private List<Phone> phoneNumbers;
    //    private List<ImClient> imClients;
    //    private List<SocialMedia> socialMedias;
    private URL website;
    private List<String> taglines;
    private String biography;
    private String organization;
    private String occupation;
    private List<String> interests;
    private List<String> skills;

    //    private List<Relation> relations;
    @DateFormat("dd/MM/yyyy")
    private java.sql.Date joinDate;
    private java.sql.Date anniversary;
    private String maritalStatus;

    private transient List<String> creditCards;

    private String userAgent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public List<URL> getCoverphotos() {
        return coverphotos;
    }

    public void setCoverphotos(List<URL> coverphotos) {
        this.coverphotos = coverphotos;
    }

    public List<URL> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<URL> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getMemberSince() {
        return memberSince;
    }

    public void setMemberSinceDate(Date memberSince) {
        this.memberSince = memberSince;
    }

    public String getFavColor() {
        return favColor;
    }

    public void setFavColor(String favColor) {
        this.favColor = favColor;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public URL getWebsite() {
        return website;
    }

    public void setWebsite(URL website) {
        this.website = website;
    }

    public List<String> getTaglines() {
        return taglines;
    }

    public void setTaglines(List<String> taglines) {
        this.taglines = taglines;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public java.sql.Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(java.sql.Date joinDate) {
        this.joinDate = joinDate;
    }

    public java.sql.Date getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(java.sql.Date anniversary) {
        this.anniversary = anniversary;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<String> creditCards) {
        this.creditCards = creditCards;
    }

    public void addName(String name) {
        if (names == null)
            names = new LinkedList<>();
        names.add(name);
    }

    public void addNickName(String nickname) {
        if (nicknames == null)
            nicknames = new LinkedList<>();
        nicknames.add(nickname);
    }

    public void addCoverPhoto(URL coverphoto) {
        if (coverphotos == null)
            coverphotos = new LinkedList<>();
        coverphotos.add(coverphoto);
    }

    public void addProfilePicture(URL profilePicture) {
        if (profilePictures == null)
            profilePictures = new LinkedList<>();
        profilePictures.add(profilePicture);
    }

    //    public void addAddress(Address address){
    //        if(addresses==null)addresses = new LinkedList<>();
    //        addresses.add(address);
    //    }

    public void addEmailAddress(String emailAddress) {
        if (emailAddresses == null)
            emailAddresses = new LinkedList<>();
        emailAddresses.add(emailAddress);
    }

    //    public void addPhoneNumber(Phone phoneNumber){
    //        if(phoneNumbers==null)phoneNumbers = new LinkedList<>();
    //        phoneNumbers.add(phoneNumber);
    //    }

    //    public void addImClient(ImClient imClient){
    //        if(imClients==null)imClients = new LinkedList<>();
    //        imClients.add(imClient);
    //    }

    //    public void addSocialMedia(SocialMedia socialMedia){
    //        if(socialMedias==null)socialMedias = new LinkedList<>();
    //        socialMedias.add(socialMedia);
    //    }

    public void addTagline(String tagline) {
        if (taglines == null)
            taglines = new LinkedList<>();
        taglines.add(tagline);
    }

    public void addInterest(String interest) {
        if (interests == null)
            interests = new LinkedList<>();
        interests.add(interest);
    }

    public void addSkill(String skill) {
        if (skills == null)
            skills = new LinkedList<>();
        skills.add(skill);
    }

    //    public void addRelationship(Relation relation){
    //        if(relations==null)relations = new LinkedList<>();
    //        relations.add(relation);
    //    }

    public void addCreditCard(String card) {
        if (creditCards == null)
            creditCards = new LinkedList<>();
        creditCards.add(card);
    }
}
