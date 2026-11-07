package com.xue.aiagent_me.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义文档加载器
 * 负责读取所有的 Markdown 文档并转换为 Document 列表
 */
@Component
@Slf4j
public class AppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public AppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }


    public List<Document> loadMarkdowns() throws IOException {
        List<Document> allDocuments = new ArrayList<>();

        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                //转换
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(
                        resource,
                        MarkdownDocumentReaderConfig.builder() .withHorizontalRuleCreateDocument(true)
                                .withAdditionalMetadata("filename", resource.getFilename())
                                .build()
                );
                allDocuments.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }

        return allDocuments;
    }

}
