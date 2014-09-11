/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.service;

import dk.dma.msinm.model.Category;
import dk.dma.msinm.model.Message;
import dk.dma.msinm.common.model.DataFilter;
import dk.dma.msinm.vo.MessageVo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates the message search result.
 * <p>
 * The search result is paged, as per the {@code MessageSearchParams},
 * so, the messages returned constitutes a subset of the {@code total}
 * number of messages starting af the {@code startIndex}.
 * <p>
 * If the request number of messages exceeds a System-wide maximum, the
 * overflowed flag will be set, and no messages will be returned.
 *
 * This is e.g. used in Map view mode to show the MSI-NM background layer.
 */
public class MessageSearchResult implements Serializable {

    List<MessageVo> messages = new ArrayList<>();
    int startIndex;
    int total;
    boolean overflowed;

    /**
     * Add a list of messages to the search result
     * @param messages the messages to add
     * @param bookmarkIds the bookmarked messages
     * @param firingExerciseCategory the Firing Exercise cateogry
     * @param dataFilter what type of data to copy from the entity
     */
    public void addMessages(List<Message> messages, Set<Integer> bookmarkIds, Category firingExerciseCategory, DataFilter dataFilter) {
        messages.forEach(msg -> {
            MessageVo messageVo = new MessageVo(msg, dataFilter);
            messageVo.setBookmarked(bookmarkIds.contains(messageVo.getId()));
            if (dataFilter.include("Message.firingExercise")) {
                messageVo.setFiringExercise(
                        msg.getCategories().stream()
                                .anyMatch(cat -> cat.getId().equals(firingExerciseCategory.getId())));
            }
            this.messages.add(messageVo);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MessageSearchResult{" +
                "messages no=" + messages.size() +
                ", startIndex=" + startIndex +
                ", total=" + total +
                ", overflowed=" + overflowed +
                '}';
    }

    public List<MessageVo> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageVo> messages) {
        this.messages = messages;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isOverflowed() {
        return overflowed;
    }

    public void setOverflowed(boolean overflowed) {
        this.overflowed = overflowed;
    }
}
