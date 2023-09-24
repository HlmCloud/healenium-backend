package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import lombok.Data;
import lombok.experimental.Accessors;
import org.openqa.selenium.By;

import java.util.List;

@Data
@Accessors(chain = true)
public class ReferenceDataDto {

    private List<Node> path;
    private String table;
    private Node tableNode;
    private String tableCssSelector;
    private String url;

}
