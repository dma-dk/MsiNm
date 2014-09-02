package dk.dma.msinm.model;

import dk.dma.msinm.common.model.BaseEntity;
import dk.dma.msinm.user.User;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Defines a bookmarked message for a specific user
 */
@Entity
@NamedQueries({
    @NamedQuery(name  = "Bookmark.findByUser",
                query = "select b from Bookmark b where b.user = :user"),
    @NamedQuery(name  = "Bookmark.findByUserEmail",
                query = "select b from Bookmark b where lower(b.user.email) = lower(:email)"),
    @NamedQuery(name  = "Bookmark.findByUserEmailAndMessageId",
                query = "select b from Bookmark b where lower(b.user.email) = lower(:email) and b.message.id = :messageId")
})
public class Bookmark extends BaseEntity<Integer> {

    @ManyToOne
    User user;

    @ManyToOne
    Message message;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
