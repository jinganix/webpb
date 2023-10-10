import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample6 {

    @JsonIgnore
    @WebpbRequestMapping
    void sample(FooRequest request) {
    }
}
