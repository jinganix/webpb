import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class Sample3 {

    @WebpbRequestMapping("sample3")
    void sample(FooRequest request) {
    }
}
