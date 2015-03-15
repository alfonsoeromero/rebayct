# ReBayCT #

## Introduction ##

**ReBayCT** ('Redes Bayesianas para Clasificación en Tesauros', literally in Spanish "Bayesian networks for classification from a Thesaurus") is a console-based tool for performing experiments in Thesaurus-based indexing, that is to say, Text Categorization over the set of descriptors of a thesaurus. For more information in this problem, see **1**.

There are several classifiers implemented in this software. Two baseline (VSM and hierarchical VSM) and one algorithm based in Bayesian networks with versions for unsupervised classification and also supervised. If you use them, please consider citing **2** and **3**

**1** L. M. de Campos, J. M. Fernández-Luna, J. F. Huete, A. E. Romero, _Thesaurus Based Automatic Indexing_, book chapter in Handbook of Research on Text and Web Mining Technologies. Ed. Idea Group, Inc. USA, 2009, ISBN: 978-1-59904-990-8. Available online at http://www.cs.rhul.ac.uk/~aeromero/pdf/thesaurus.pdf.

**2** L. M. de Campos, A. E. Romero, _Bayesian Network Models for Hierarchical Text Classification from a Thesaurus_, Int. J. Approx. Reasoning 50(7): 932-944 (2009). Available online at http://www.cs.rhul.ac.uk/~aeromero/pdf/ijar09-thesaurus.pdf.

**3** L. M. de Campos, J. M. Fernández-Luna, J. F. Huete, A. E. Romero, _Automatic Indexing from a Thesaurus Using Bayesian Networks: Application to the Classification of Parliamentary Initiatives_. ECSQARU 2007: 865-877. In: Lecture Notes in Computer Science 4724 Springer 2007, ISBN 978-3-540-75255-4. Available online at http://www.cs.rhul.ac.uk/~aeromero/pdf/lncs07-ecsqaru-thesaurus.pdf.

## The software ##
ReBayCT is written entirely in Java without needing any additional library. The snowball stemmers were included in the sources (note that their BSD license allows that).

## License ##
All the code is licensed under the GPLv3. The license is included in the "dowloads" folder, and can be read online here http://www.gnu.org/licenses/gpl.html.

## Authors ##
[Alfonso E. Romero](http://www.cs.rhul.ac.uk/~aeromero) (aeromero AT cs.rhul.ac.uk) and [Luis M. de Campos](http://decsai.ugr.es/~lci) (lci AT decsai.ugr.es). Please mail Alfonso E. Romero for any query on this software, and consider citing at least one of the papers listed above if you use this tool.
