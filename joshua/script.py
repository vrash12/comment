import os
import re
import numpy as np

from langchain.schema import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.embeddings import HuggingFaceEmbeddings
from langchain.chains import RetrievalQA
from sklearn.decomposition import PCA
from transformers import AutoTokenizer, AutoModelForCausalLM, pipeline
from langchain.llms import HuggingFacePipeline

# Import FAISS directly and build index manually
import faiss
from langchain.vectorstores import FAISS

########################################
# Step 1: Load and Preprocess the Text
########################################

text_file_path = "Chapter_I.txt"

with open(text_file_path, 'r', encoding='utf-8') as f:
    raw_text = f.read()

# Basic cleanup
raw_text = re.sub(r'\s+', ' ', raw_text).strip()

# Split text into chunks
text_splitter = RecursiveCharacterTextSplitter(
    chunk_size=1000,
    chunk_overlap=100,
    separators=["\n\n", "\n", " ", ""]
)
docs = text_splitter.split_text(raw_text)
documents = [Document(page_content=chunk) for chunk in docs]

########################################
# Step 2: Embedding and PCA
########################################

embedding_model_name = "sentence-transformers/all-MiniLM-L6-v2"
embeddings = HuggingFaceEmbeddings(model_name=embedding_model_name)

doc_texts = [d.page_content for d in documents]
doc_embeddings = embeddings.embed_documents(doc_texts)  # Original high-dim embeddings (e.g., 384 dim)

# Reduce embedding dimensionality using PCA
pca_dim = 20
pca = PCA(n_components=pca_dim)
reduced_embeddings = pca.fit_transform(doc_embeddings)  # Now each vector is 20-dimensional

########################################
# Step 3: Create a FAISS vector store with Reduced Embeddings
########################################

dimension = pca_dim  # 20 in this example
index = faiss.IndexFlatL2(dimension)
index.add(reduced_embeddings.astype(np.float32))  # Add reduced embeddings to index

# Create a FAISS store from the index and the documents
docstore = {str(i): documents[i] for i in range(len(documents))}
index_to_docstore_id = {i: str(i) for i in range(len(documents))}

faiss_store = FAISS(
    embedding_function=None,  # Not needed since we already have embeddings
    index=index,
    docstore=docstore,
    index_to_docstore_id=index_to_docstore_id
)

########################################
# Step 4: Set up a Local LLM
########################################

model_name = "meta-llama/Llama-2-7b-chat-hf"
tokenizer = AutoTokenizer.from_pretrained(model_name, use_fast=True)
model = AutoModelForCausalLM.from_pretrained(
    model_name,
    device_map="auto",
    load_in_8bit=True
)

pipe = pipeline(
    "text-generation",
    model=model,
    tokenizer=tokenizer,
    max_length=1024,
    temperature=0.0,
    do_sample=False
)

llm = HuggingFacePipeline(pipeline=pipe)

########################################
# Step 5: RAG Setup
########################################

retriever = faiss_store.as_retriever(search_type="similarity", search_kwargs={"k":3})
qa_chain = RetrievalQA.from_chain_type(llm=llm, chain_type="stuff", retriever=retriever, verbose=True)

########################################
# Step 6: Test the RAG System
########################################

query = "What are the main themes discussed in this commentary of Joshua Chapter 1?"
response = qa_chain.run(query)
print("Query:", query)
print("Response:", response)
