/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

/**
 *
 * @author asus-z
 */
public class Book {
    private String id;
    private String title;
    private String availability;
    private String language;
    private String authors;
    private String publicationInformation;
    private String edition;
    private String publicationDate;
    private String documentType;
    private String contentNotes;

    public Book(String id, String title, String availability, String language, String authors, String publicationInformation, String edition, String publicationDate, String documentType, String contentNotes) {
        this.id = id;
        this.title = title;
        this.availability = availability;
        this.language = language;
        this.authors = authors;
        this.publicationInformation = publicationInformation;
        this.edition = edition;
        this.publicationDate = publicationDate;
        this.documentType = documentType;
        this.contentNotes = contentNotes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAvailability() {
        return availability;
    }

    public String getLanguage() {
        return language;
    }

    public String getAuthors() {
        return authors;
    }

    public String getPublicationInformation() {
        return publicationInformation;
    }

    public String getEdition() {
        return edition;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getContentNotes() {
        return contentNotes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public void setPublicationInformation(String publicationInformation) {
        this.publicationInformation = publicationInformation;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setContentNotes(String contentNotes) {
        this.contentNotes = contentNotes;
    }

    @Override
    public String toString() {
        return "Book{" + "id=" + id + ", title=" + title + ", availability=" + availability + ", language=" + language + ", authors=" + authors + ", publicationInformation=" + publicationInformation + ", edition=" + edition + ", publicationDate=" + publicationDate + ", documentType=" + documentType + ", contentNotes=" + contentNotes + '}';
    }
    
}
