import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample9 {

    @WebpbRequestMapping(path = "/custom")
    void sample(FooRequest request) {
    }
}
