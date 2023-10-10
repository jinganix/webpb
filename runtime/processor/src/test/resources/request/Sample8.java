import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public enum Sample8 {
    A;

    @WebpbRequestMapping
    void sample(FooRequest request) {
    }
}
