package edu.stanford.slac.core_work_management.repository;

import com.mongodb.client.result.UpdateResult;
import edu.stanford.slac.core_work_management.model.Attachment;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Log4j2
@Repository
@AllArgsConstructor
public class AttachmentRepositoryImpl implements AttachmentRepositoryCustom{
    final private MongoTemplate mongoTemplate;
    @Override
    public void setPreviewID(String id, String previewID) {
        Query q = new Query();
        q.addCriteria(
                Criteria.where("id").is(id)
        );
        Update u = new Update();
        u.set("previewID", previewID);
        UpdateResult ur = mongoTemplate.updateFirst(q, u, Attachment.class);
        log.debug("Set preview id update operation {}", ur.getModifiedCount()==1);
    }

    @Override
    public void setMiniPreview(String id, byte[] byteArray) {
        Query q = new Query();
        q.addCriteria(
                Criteria.where("id").is(id)
        );
        Update u = new Update();
        u.set("miniPreview", byteArray);
        UpdateResult ur = mongoTemplate.updateFirst(q, u, Attachment.class);
        log.debug("Set mini preview update operation {}", ur.getModifiedCount()==1);
    }

    @Override
    public void setPreviewState(String id, Attachment.PreviewProcessingState state) {
        Query q = new Query();
        q.addCriteria(
                Criteria.where("id").is(id)
        );
        Update u = new Update();
        u.set("previewState", state);

        UpdateResult ur = mongoTemplate.updateFirst(q, u, Attachment.class);
        log.debug("Set preview state update operation {}", ur.getModifiedCount()==1);
    }

    @Override
    public Attachment.PreviewProcessingState getPreviewState(String id) {
        Query q = new Query();
        q.addCriteria(
                Criteria.where("id").is(id)
        ).fields().include("previewState");
        var a = Optional.ofNullable(
                mongoTemplate.findOne(q, Attachment.class)
        );
        return a.orElseThrow().getPreviewState();
    }

    @Override
    public void setInUseState(String id, Boolean inUse) {
        Query q = new Query();
        q.addCriteria(
                Criteria.where("id").is(id)
        );
        Update u = new Update();
        u.set("inUse", inUse);

        UpdateResult ur = mongoTemplate.updateFirst(q, u, Attachment.class);
        log.debug("Set 'in use' state update operation {}", ur.getModifiedCount()==1);
    }
}
