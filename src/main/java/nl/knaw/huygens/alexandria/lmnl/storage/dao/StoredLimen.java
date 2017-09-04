package nl.knaw.huygens.alexandria.lmnl.storage.dao;

import static com.sleepycat.persist.model.Relationship.ONE_TO_MANY;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class StoredLimen {
  @PrimaryKey(sequence = "limen_pk_sequence")
  private long id;

  @SecondaryKey(relate = ONE_TO_MANY, relatedEntity = StoredTextNode.class)
  private List<Long> textNodeIds = new ArrayList<>();

  @SecondaryKey(relate = ONE_TO_MANY)
  private List<Long> textRangeIds = new ArrayList<>();

  public long getId() {
    return id;
  }

  public List<Long> getTextNodeIds() {
    return textNodeIds;
  }

  public void setTextNodeIds(List<Long> textNodeIds) {
    this.textNodeIds = textNodeIds;
  }

  public List<Long> getTextRangeIds() {
    return textRangeIds;
  }

  public void setTextRangeIds(List<Long> textRangeIds) {
    this.textRangeIds = textRangeIds;
  }
}